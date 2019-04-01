package uk.co.brightec.kbarcode.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class ContactInfo(
    val addresses: List<Address>,
    val emails: List<Email>,
    val name: PersonName?,
    val organization: String?,
    val phones: List<Phone>,
    val title: String?,
    val urls: List<String>?
) {

    internal constructor(fbEvent: FirebaseVisionBarcode.ContactInfo) : this(
        addresses = fbEvent.addresses.map { Address(it) },
        emails = fbEvent.emails.map { Email(it) },
        name = fbEvent.name?.convert(),
        organization = fbEvent.organization,
        phones = fbEvent.phones.map { Phone(it) },
        title = fbEvent.title,
        urls = fbEvent.urls?.toList()
    )
}

internal fun FirebaseVisionBarcode.ContactInfo.convert() = ContactInfo(this)
