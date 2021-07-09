package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class DrivingLicense(
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

    internal constructor(mlDrivingLicense: MlBarcode.DriverLicense) : this(
        addressCity = mlDrivingLicense.addressCity,
        addressState = mlDrivingLicense.addressState,
        addressStreet = mlDrivingLicense.addressStreet,
        addressZip = mlDrivingLicense.addressZip,
        birthDate = mlDrivingLicense.birthDate,
        documentType = mlDrivingLicense.documentType,
        expiryDate = mlDrivingLicense.expiryDate,
        firstName = mlDrivingLicense.firstName,
        gender = mlDrivingLicense.gender,
        issueDate = mlDrivingLicense.issueDate,
        issuingCountry = mlDrivingLicense.issuingCountry,
        lastName = mlDrivingLicense.lastName,
        licenseNumber = mlDrivingLicense.licenseNumber,
        middleName = mlDrivingLicense.middleName
    )
}

internal fun MlBarcode.DriverLicense.convert() = DrivingLicense(this)
