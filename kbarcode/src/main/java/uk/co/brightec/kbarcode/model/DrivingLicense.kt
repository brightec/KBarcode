package uk.co.brightec.kbarcode.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class DrivingLicense(
    val addressCity: String?,
    val addressState: String?,
    val addressStreet: String?,
    val addressZip: String?,
    val birthDate: String?,
    val documentType: String?,
    val expiryDate: String?,
    val firstName: String?,
    val gender: String?,
    val issueDate: String?,
    val issuingCountry: String?,
    val lastName: String?,
    val licenseNumber: String?,
    val middleName: String?
) {

    internal constructor(fbDrivingLicense: FirebaseVisionBarcode.DriverLicense) : this(
        addressCity = fbDrivingLicense.addressCity,
        addressState = fbDrivingLicense.addressState,
        addressStreet = fbDrivingLicense.addressStreet,
        addressZip = fbDrivingLicense.addressZip,
        birthDate = fbDrivingLicense.birthDate,
        documentType = fbDrivingLicense.documentType,
        expiryDate = fbDrivingLicense.expiryDate,
        firstName = fbDrivingLicense.firstName,
        gender = fbDrivingLicense.gender,
        issueDate = fbDrivingLicense.issueDate,
        issuingCountry = fbDrivingLicense.issuingCountry,
        lastName = fbDrivingLicense.lastName,
        licenseNumber = fbDrivingLicense.licenseNumber,
        middleName = fbDrivingLicense.middleName
    )
}

internal fun FirebaseVisionBarcode.DriverLicense.convert() = DrivingLicense(this)
