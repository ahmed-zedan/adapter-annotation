package com.example.zedan.autoadapter_processor.codegen

import com.example.zedan.autoadapter_processor.models.ModelData
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class AdapterCodeBuilder (
    private val adapterName: String,
    private val data: ModelData
){
    private val viewHolderName = "ViewHolder" // 1
    private val viewHolderClassName = ClassName(data.packageName, viewHolderName) // 2
    private val viewHolderQualifiedClassName = ClassName(data.packageName,
        "$adapterName.$viewHolderName"
    ) // 3
    private val modelClassName = ClassName(data.packageName, data.modelName) // 4
    private val itemsListClassName = ClassName("kotlin.collections", "List") // 5
        .parameterizedBy(modelClassName)
    private val textViewClassName = ClassName("android.widget", "TextView") // 6

    fun build(): TypeSpec = TypeSpec.classBuilder(adapterName)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter("items", itemsListClassName)
            .build()
        )
        .superclass(ClassName("androidx.recyclerview.widget.RecyclerView", "Adapter")
            .parameterizedBy(viewHolderQualifiedClassName)

        )
        .addProperty(PropertySpec.builder("items", itemsListClassName)
            .addModifiers(KModifier.PRIVATE)
            .initializer("items")
            .build()
        )
        .addBaseMethods()
        .addViewHolderType()
        .build()

    private fun TypeSpec.Builder.addBaseMethods(): TypeSpec.Builder = apply {
        addFunction(FunSpec.builder("getItemCount")
            .addModifiers(KModifier.OVERRIDE)
            .returns(INT)
            .addStatement("return items.size")
            .build()
        )

        addFunction(FunSpec.builder("onCreateViewHolder")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("parent", ClassName("android.view", "ViewGroup"))
            .addParameter("viewType", INT)
            .returns(viewHolderQualifiedClassName)
            .addStatement("val view = android.view.LayoutInflater.from(parent.context).inflate(%L, parent, false)",
                data.layoutId)
            .addStatement("return $viewHolderName(view)")
            .build()
        )

        addFunction(FunSpec.builder("onBindViewHolder")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("viewHolder", viewHolderQualifiedClassName)
            .addParameter("position", INT)
            .addStatement("viewHolder.bind(items[position])")
            .build()
        )
    }

    private fun TypeSpec.Builder.addViewHolderType(): TypeSpec.Builder = addType(
        TypeSpec.classBuilder(viewHolderClassName)
            .primaryConstructor(FunSpec.constructorBuilder()
                .addParameter("itemView", ClassName("android.view", "View"))
                .build()
            )
            .superclass(ClassName("androidx.recyclerview.widget.RecyclerView",
            "ViewHolder"))
            .addSuperclassConstructorParameter("itemView")
            .addBindMethod()
            .build()
    )

    private fun TypeSpec.Builder.addBindMethod(): TypeSpec.Builder = addFunction(
        FunSpec.builder("bind")
            .addParameter("item", modelClassName)
            .apply {
                data.viewHolderBindingData.forEach {
                        addStatement("" +
                                "itemView.findViewById<%T>(%L).text = item.%L",
                        textViewClassName, it.viewId, it.fieldName)
                    }
            }
            .build()
    )
}