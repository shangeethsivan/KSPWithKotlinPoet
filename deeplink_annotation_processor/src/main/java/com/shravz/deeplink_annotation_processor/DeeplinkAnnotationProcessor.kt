/*
package com.shravz.deeplink_annotation_processor

import com.shravz.deeplink_annotation.Deeplink
import com.squareup.kotlinpoet.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic


class DeeplinkAnnotationProcessor : AbstractProcessor() {


    private val METHOD_NAME = "handleDeeplink"
//    private val classBoolean = ClassName.get(Boolean::class.java)
//    private val classString = ClassName.get(String::class.java)
//    private val classNavController = ClassName.get("androidx.navigation", "NavController")

    private var filer: Filer? = null
    private var messager: Messager? = null
    private var domainsAndRoutes: HashMap<String, String>? = null
    private var elements: Elements? = null

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Deeplink::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnvironment.messager
        domainsAndRoutes = HashMap()
        elements = processingEnvironment.elementUtils
        filer = processingEnvironment.filer
    }

    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment
    ): Boolean {

        //1. Find all annotated element

        for (element in roundEnvironment.getElementsAnnotatedWith(Deeplink::class.java)) {
            if (element.kind !== ElementKind.CLASS) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Can be applied to class."
                )
                return true
            }
            val typeElement = element as TypeElement
            domainsAndRoutes?.put(
                typeElement.simpleName.toString(),
                elements?.getPackageOf(typeElement)?.qualifiedName.toString()
            )
        }

        val fileBuilder = FileSpec.builder("", "DeeplinkProcessor")
            .addType(
                TypeSpec.classBuilder("DeeplinkProcessor")
                    .build()
            )

        val functionBuilder = FunSpec.builder("processDeeplink")
            .addParameter(
                ParameterSpec(
                    "navController", ClassName("androidx.navigation", "NavController")
                )
            ).addParameter(
                ParameterSpec(
                    "intentUri", ClassName("android.net", "Uri")
                )
            ).addStatement("val scheme = intentUri?.scheme")
            .addStatement("val host = intentUri?.host")
            .addStatement("if (scheme == \"domainstage\") {")
            .addStatement("|     when (requireNotNull(host)) {")

        domainsAndRoutes?.entries?.forEach {
            val domain = it.key
            val route = it.value
            functionBuilder.addStatement("|         \"$domain\" -> navController.navigate($route)")
        }
        functionBuilder.addStatement("|         else -> Log.e(\"DeeplinkProcessor\",\"Unknown Host \$domain\")")
        functionBuilder.addStatement("|     }")
        functionBuilder.addStatement("}")

        fileBuilder.addFunction(functionBuilder.build())

        fileBuilder.build().writeTo(requireNotNull(filer))

        return true
    }
}*/
