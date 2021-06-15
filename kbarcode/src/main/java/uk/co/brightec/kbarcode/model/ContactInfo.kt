package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class ContactInfo(
    val addresses: List<Address>,
    val emails: List<Email>,
    val name: PersonName?,
    val organization: String?,
    val phones: List<Phone>,
    val title: String?,
    val urls: List<String>?
) {

    internal constructor(mlEvent: MlBarcode.ContactInfo) : this(
        addresses = mlEvent.addresses.map { Address(it) },
        emails = mlEvent.emails.map { Email(it) },
        name = mlEvent.name?.convert(),
        organization = mlEvent.organization,
        phones = mlEvent.phones.map { Phone(it) },
        title = mlEvent.title,
        urls = mlEvent.urls.toList()
    )
}

internal fun MlBarcode.ContactInfo.convert() = ContactInfo(this)
