package com.example.zedan.adapterannotation

import com.example.zedan.autoadapter_annotations.AdapterModel
import com.example.zedan.autoadapter_annotations.ViewHolderBinding

@AdapterModel(R.layout.layout_person)
data class Person(
    @ViewHolderBinding(R.id.person_name) val name: String,
    @ViewHolderBinding(R.id.person_address) val address: String
)