package moe.ono.hooks

import moe.ono.dexkit.DexMethodDescriptor
import moe.ono.util.Initiator
import moe.ono.util.Logger
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal val String.clazz: Class<*>?
    get() = Initiator.load(this).also {
        if (it == null) {
            Logger.e(ClassNotFoundException("Class $this not found"))
        }
    }

internal val ArrayList<String>.clazz: Class<*>
    get() = firstNotNullOf { Initiator.load(it) }

internal val String.method: Method
    get() = DexMethodDescriptor(
        this.replace(".", "/").replace(" ", "")
    ).getMethodInstance(Initiator.getHostClassLoader())

internal fun Class<*>.method(name: String): Method? = this.declaredMethods.run {
    this.forEach {
        if (it.name == name) {
            return it
        }
    }
    Logger.w(NoSuchMethodException("No such method $name in $this"))
    null
}

internal fun Class<*>.method(name: String, returnType: Class<*>?, vararg argsTypes: Class<*>?): Method? = this.run {
    this.declaredMethods.forEach {
        if (name == it.name && returnType == it.returnType && it.parameterTypes.contentEquals(argsTypes)) {
            return it
        }
    }
    Logger.w(NoSuchMethodException("No such method $name in ${this.simpleName} with returnType $returnType and argsTypes " + argsTypes.joinToString()))
    null
}

internal fun Class<*>.methodWithSuper(name: String, returnType: Class<*>?, vararg argsTypes: Class<*>?): Method? = this.run {
    (this.declaredMethods + this.methods).forEach {
        if (name == it.name && returnType == it.returnType && it.parameterTypes.contentEquals(argsTypes)) {
            return it
        }
    }
    Logger.w(NoSuchMethodException("No such method $name in ${this.simpleName} with returnType $returnType and argsTypes " + argsTypes.joinToString()))
    null
}

internal fun Class<*>.method(
    condition: (method: Method) -> Boolean = { true }
): Method? = this.run {
    this.declaredMethods.forEach {
        if (condition(it)) {
            return it
        }
    }
    Logger.w(NoSuchMethodException("No such method in ${this.simpleName}"))
    null
}

internal fun Class<*>.method(
    size: Int,
    returnType: Class<*>?,
    condition: (method: Method) -> Boolean = { true }
): Method? = this.run {
    this.declaredMethods.forEach {
        if (it.returnType == returnType && it.parameterTypes.size == size && condition(it)) {
            return it
        }
    }
    Logger.w(NoSuchMethodException("No such method in ${this.simpleName} with returnType $returnType and argsSize $size"))
    null
}

internal fun Class<*>.method(
    name: String,
    size: Int,
    returnType: Class<*>?,
    condition: (method: Method) -> Boolean = { true }
): Method? = this.run {
    this.declaredMethods.forEach {
        if (it.name == name && it.returnType == returnType && it.parameterTypes.size == size && condition(it)) {
            return it
        }
    }
    Logger.w(NoSuchMethodException("No such method $name in ${this.simpleName} with returnType $returnType and argsSize $size"))
    null
}

internal val Member.isStatic: Boolean
    get() = Modifier.isStatic(this.modifiers)

internal val Member.isPrivate: Boolean
    get() = Modifier.isPrivate(this.modifiers)

internal val Member.isPublic: Boolean
    get() = Modifier.isPublic(this.modifiers)

internal val Member.isFinal: Boolean
    get() = Modifier.isFinal(this.modifiers)

internal val Class<*>.isAbstract: Boolean
    get() = Modifier.isAbstract(this.modifiers)
