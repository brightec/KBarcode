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

    internal constructor(fbEvent: MlBarcode.ContactInfo) : this(
        addresses = fbEvent.addresses.map { Address(it) },
        emails = fbEvent.emails.map { Email(it) },
        name = fbEvent.name?.convert(),
        organization = fbEvent.organization,
        phones = fbEvent.phones.map { Phone(it) },
        title = fbEvent.title,
        urls = fbEvent.urls.toList()
    )
}

internal fun MlBarcode.ContactInfo.convert() = ContactInfo(this)
