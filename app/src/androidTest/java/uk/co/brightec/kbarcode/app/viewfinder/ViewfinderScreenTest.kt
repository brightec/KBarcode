package uk.co.brightec.kbarcode.app.viewfinder

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.brightec.kbarcode.Barcode
import uk.co.brightec.kbarcode.app.R
import uk.co.brightec.kbarcode.app.testutil.SingleFragmentActivity

@LargeTest
internal class ViewfinderScreenTest {

    @Suppress("BooleanLiteralArgument") // Java method call
    @get:Rule
    val activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    private lateinit var barcode: MutableLiveData<Resource<Barcode>>
    private lateinit var viewModel: ViewfinderViewModel

    private lateinit var fragment: ViewfinderFragment

    @Before
    fun before() {
        barcode = MutableLiveData()
        viewModel = mock {
            on { this.barcode } doReturn barcode
        }

        fragment = ViewfinderFragment()
        fragment.viewModel = viewModel

        activityRule.activity.setFragment(fragment)
    }

    @Test
    fun nothing__barcodeLoading__showsProgress() {
        // GIVEN
        // nothing

        // WHEN
        barcode.postValue(Resource.Loading(mock()))

        // THEN
        onView(withId(R.id.progress))
            .check(matches(isDisplayed()))
    }

    @Test
    fun nothing__barcodeError__showsToast() {
        // GIVEN
        // nothing

        // WHEN
        barcode.postValue(Resource.Error(mock(), mock()))

        // THEN
        onView(withText(R.string.error_processing_barcode))
            .inRoot(isToast())
            .check(matches(isDisplayed()))
    }

    @Test
    fun barcode__barcodeSuccess__showsToast() {
        // GIVEN
        val barcode = mock<Barcode> {
            on { displayValue } doReturn "123456"
        }

        // WHEN
        this.barcode.postValue(Resource.Success(barcode))

        // THEN
        onView(withText(barcode.displayValue))
            .inRoot(isToast())
            .check(matches(isDisplayed()))
    }

    private fun isToast() = withDecorView(not(`is`(activityRule.activity.window.decorView)))
}
