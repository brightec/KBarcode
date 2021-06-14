package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

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

    internal constructor(fbDrivingLicense: MlBarcode.DriverLicense) : this(
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

internal fun MlBarcode.DriverLicense.convert() = DrivingLicense(this)
