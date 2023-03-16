package com.shravz.deeplink_annotation

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

@OptIn(KotlinPoetKspPreview::class)
internal class DeeplinkKSPProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private fun Resolver.findAnnotations(
        kClass: KClass<*>,
    ) = getSymbolsWithAnnotation(
        kClass.qualifiedName.toString()
    ).filterIsInstance<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val deeplinkClasses: Sequence<KSClassDeclaration> =
            resolver.findAnnotations(Deeplink::class)
        if (!deeplinkClasses.iterator().hasNext()) return emptyList()

        val domainsAndRoutes = mutableMapOf<String, String>()
        deeplinkClasses.iterator().forEach { kSClass ->
            val annotation = kSClass.annotations.first { annotation ->
                annotation.shortName.asString() == "Deeplink"
            }
            domainsAndRoutes[annotation.arguments[0].value as String] =
                annotation.arguments[1].value as String
        }


        val fileBuilder = FileSpec.builder("", "DeeplinkProcessor")

        val functionBuilder = FunSpec.builder("processDeeplink")
            .addParameter(
                ParameterSpec(
                    "navController", ClassName("androidx.navigation", "NavController")
                )
            ).addParameter(
                ParameterSpec(
                    "intentUri", ClassName("android.net", "Uri")
                )
            ).addStatement("val scheme = intentUri.scheme")
            .addStatement("val host = intentUri.host")
            .addStatement("if (scheme == \"domainstage\") {")
            .addStatement("     when (requireNotNull(host)) {")

        domainsAndRoutes.entries.forEach {
            val domain = it.key
            val route = it.value
            functionBuilder.addStatement("        \"$domain\" -> navController.navigate(\"$route\")")
        }
        functionBuilder.addStatement("         else -> Log.e(\"DeeplinkProcessor\",\"Unknown Host \$host\")")
        functionBuilder.addStatement("     }")
        functionBuilder.addStatement("}")


        val klass = TypeSpec.classBuilder("DeeplinkProcessor")
            .addFunction(functionBuilder.build())
            .build()

        fileBuilder.addType(klass)
        fileBuilder.addImport("android.util", "Log")

        val fileSpec = fileBuilder.build()
        fileSpec.writeTo(environment.codeGenerator, Dependencies(false))

        return (deeplinkClasses).filterNot { it.validate() }.toList()
    }

}