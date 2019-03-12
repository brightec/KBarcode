package uk.co.brightec.kbarcode.app.util

/**
 * This annotation allows us to open some classes for mocking purposes while they are final in
 * release builds.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
internal annotation class OpenClass

/**
 * Annotate a class with [OpenForTesting] if you want it to be extendable in debug builds.
 */
@OpenClass
@Target(AnnotationTarget.CLASS)
internal annotation class OpenForTesting
