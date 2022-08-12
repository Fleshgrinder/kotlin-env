package com.fleshgrinder.env

import java.io.Serializable
import java.util.Collections.*

public class Env internal constructor(@JvmField public val vars: Map<String, String>) : Serializable {
    public operator fun contains(key: String): Boolean =
        key in vars

    public operator fun get(key: String): String =
        vars[key] ?: throw NoSuchElementException("Missing required environment variable $key")

    public inline fun get(key: String, lazyMessage: (key: String) -> String): String =
        vars[key] ?: throw NoSuchElementException(lazyMessage(key))

    public fun getOrNull(key: String): String? =
        vars[key]

    public operator fun plus(that: Env): Env =
        this + that.vars

    public operator fun plus(thatVars: Map<String, String>): Env {
        val thisVars = this.vars
        val thisSize = thisVars.size
        val thatSize = thatVars.size
        if (thisSize == 0 || thatSize == 0) return this

        val vars = HashMap<String, String>(thisSize + thatSize, 1f)
        vars += thisVars
        vars += thatVars
        return Env(unmodifiableMap(vars))
    }

    public operator fun plus(kv: Pair<String, String>): Env =
        Env(unmodifiableMap<String?, String?>(HashMap(vars.size + 1, 1f)).apply {
            this += vars
            this += kv
        })

    override fun equals(other: Any?): Boolean =
        this === other || vars == (other as? Env)?.vars

    override fun hashCode(): Int =
        vars.hashCode()

    override fun toString(): String =
        "Env$vars"

    public companion object
}

public fun emptyEnv(): Env =
    Env(emptyMap())

public fun processEnv(): Env =
    Env(System.getenv())

public fun envOf(k: String, v: String): Env =
    Env(singletonMap(k, v))

public fun envOf(kv: Map.Entry<String, String>): Env =
    envOf(kv.key, kv.value)

public fun envOf(kv: Pair<String, String>): Env =
    envOf(kv.first, kv.second)

public fun envOf(kv: Pair<String, String>, vararg kvs: Pair<String, String>): Env =
    Env(unmodifiableMap(HashMap<String, String>(1 + kvs.size, 1f)).apply {
        this += kv
        this += kvs
    })

public fun envOf(vars: Map<String, String>): Env =
    Env(unmodifiableMap(HashMap(vars)))

public fun envOf(vars: Array<out Pair<String, String>>): Env =
    Env(unmodifiableMap(HashMap<String, String>(vars.size, 1f)).apply {
        this += vars
    })

public fun envOf(vars: Iterable<Pair<String, String>>): Env {
    val entries = HashMap<String, String>()
    for (`var` in vars) {
        entries += `var`
    }
    return when (entries.size) {
        0 -> emptyEnv()
        1 -> envOf(entries.entries.iterator().next())
        else -> Env(unmodifiableMap(entries))
    }
}
