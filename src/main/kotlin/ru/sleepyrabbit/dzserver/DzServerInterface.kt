package ru.sleepyrabbit.dzserver

interface DataCoder<T> {
    fun decode(data: ByteArray) : T
    fun encode(data: T) : ByteArray
}

abstract class Router<T> {

    protected val routes = mutableMapOf<String, (Any) -> Any>()

    private var preIntermediateProcessor:LinkedIntermediateProcessor? = null
    private var postIntermediateProcessor:LinkedIntermediateProcessor? = null

    data class LinkedIntermediateProcessor(
        val processor: (Any?) -> Any,
        var next: LinkedIntermediateProcessor?){

        fun process(data: Any?): Any {
            val result = processor(data)
            return next?.process(result) ?: result
        }

        fun last(): LinkedIntermediateProcessor{
           return next?.last() ?: this
        }
    }

    abstract fun route(message: T): T

    fun bindRoute(key: String, action: (Any) -> Any): Router<T> {
        routes[key] = action
        return this;
    }

    fun unbindRoute(key: String): Router<T> {
        routes.remove(key)
        return this;
    }

    fun addPreProcessor(processor: (Any?) -> Any): Router<T> {
        if (preIntermediateProcessor == null) {
            preIntermediateProcessor = LinkedIntermediateProcessor(processor, null)
        } else {
            preIntermediateProcessor!!.last().next = LinkedIntermediateProcessor(processor, null)
        }

        return this;
    }

    fun addPostProcessor(processor: (Any?) -> Any): Router<T> {
        if (postIntermediateProcessor == null) {
            postIntermediateProcessor = LinkedIntermediateProcessor(processor, null)
        } else {
            postIntermediateProcessor!!.last().next = LinkedIntermediateProcessor(processor, null)
        }
        return this;
    }

    fun preProcess(data: T): Any {
        return preIntermediateProcessor!!.process(data!!)
    }

    fun postProcess(data:Any?): T {
        return postIntermediateProcessor!!.process(data) as T
    }

    private inline fun <reified T> validateProcessors(){
        if (preIntermediateProcessor!!.processor
                .javaClass
                .methods[0]
                .parameterTypes[0] !is T
        )
            throw InvalidProcessorConfigurationException(preIntermediateProcessor!!.processor.javaClass.name)

        if (postIntermediateProcessor!!.last().processor
                .javaClass
                .methods[0]
                .returnType !is T)
            throw InvalidProcessorConfigurationException(postIntermediateProcessor!!.last().javaClass.name)
    }

}