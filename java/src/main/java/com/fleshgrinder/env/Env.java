package com.fleshgrinder.env;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import kotlin.annotations.jvm.ReadOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableMap;

/**
 * The {@code Env} class is an <em>immutable</em> data structure that provides access to
 * <a href="https://en.wikipedia.org/wiki/Environment_variable">environment variables</a>. Usually those of the current
 * process, but this is opaque to the consumer of an {@code Env} instance.
 */
public final class Env implements Serializable {
    /**
     * Gets the variables in this environment as immutable map.
     * <p>
     * The map itself is unmodifiable and since both keys and values stored in the map are themselves immutable strings it is deeply immutable. Calling any
     * mutating function on this map is going to result in an {@link UnsupportedOperationException} being thrown.
     */
    public final @NotNull @ReadOnly @Unmodifiable Map<@NotNull String, @NotNull String> vars;

    private Env(final @NotNull @ReadOnly @Unmodifiable Map<@NotNull String, @NotNull String> vars) {
        this.vars = vars;
    }

    /**
     * Constructs a new {@code Env} with the vars from {@link System#getenv()}.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull Env processEnv() {
        return new Env(System.getenv());
    }

    /**
     * Constructs a new {@code Env} without any vars.
     */
    @Contract(value = "-> new", pure = true)
    public static @NotNull Env emptyEnv() {
        return new Env(emptyMap());
    }

    /**
     * Constructs a new {@code Env} with a single var.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Env envOf(final @NotNull String k, final @NotNull String v) {
        return new Env(singletonMap(k, v));
    }

    /**
     * Constructs a new {@code Env} with <i>1 + n</i> vars.
     */
    public static @NotNull Env envOf(final @NotNull String k, final @NotNull String v, final @NotNull String... vars) {
        final int length = vars.length;
        if (length % 2 != 0) {
            throw new IllegalArgumentException("Var count MUST be even, got: " + (2 + length));
        }
        return new Env(unmodifiableMap(new HashMap<String, String>(2 + length, 1f) {{
            put(k, v);
            for (int i = 0, j = 1; j < length; i++, j++) {
                put(vars[i], vars[j]);
            }
        }}));
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Env envOf(final @NotNull @ReadOnly Map.Entry<@NotNull String, @NotNull String> var) {
        return envOf(var.getKey(), var.getValue());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Env envOf(final @NotNull @ReadOnly @Unmodifiable Map<@NotNull String, @NotNull String> vars) {
        switch (vars.size()) {
            case 0:
                return emptyEnv();
            case 1:
                return envOf(vars.entrySet().iterator().next());
            default:
                return new Env(unmodifiableMap(new HashMap<>(vars)));
        }
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Env envOf(final @NotNull Consumer<@NotNull Map<@NotNull String, @NotNull String>> builder) {
        return envOf(new HashMap<String, String>() {{
            builder.accept(this);
        }});
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Env envOf(
        final @Range(from = 0, to = Integer.MAX_VALUE) int capacity,
        final @NotNull Consumer<@NotNull Map<@NotNull String, @NotNull String>> builder
    ) {
        return envOf(new HashMap<String, String>(capacity) {{
            builder.accept(this);
        }});
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull Env envOf(
        final @Range(from = 0, to = Integer.MAX_VALUE) int capacity,
        final @Range(from = 0, to = 1) float loadFactor,
        final @NotNull Consumer<@NotNull Map<@NotNull String, @NotNull String>> builder
    ) {
        return envOf(new HashMap<String, String>(capacity, loadFactor) {{
            builder.accept(this);
        }});
    }

    @Contract(pure = true)
    public boolean contains(final @NotNull String key) {
        return vars.containsKey(key);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Env copy(final @NotNull Consumer<@NotNull Map<@NotNull String, @NotNull String>> action) {
        return new Env(new HashMap<String, String>(this.vars) {{
            action.accept(this);
        }});
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final @Nullable Object other) {
        return this == other || (other instanceof Env && vars.equals(((Env) other).vars));
    }

    @Contract(pure = true)
    public @NotNull String get(final @NotNull String key) {
        final String value = vars.get(key);
        if (value == null) {
            throw new NoSuchElementException("Missing required environment variable " + key);
        }
        return value;
    }

    @Contract(pure = true)
    public @NotNull String getOrDefault(final @NotNull String key, final @NotNull String fallback) {
        return vars.getOrDefault(key, fallback);
    }

    @Contract(pure = true)
    public @Nullable String getOrNull(final @NotNull String key) {
        return vars.get(key);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return vars.hashCode();
    }

    @Contract(pure = true)
    public @NotNull Env plus(final @NotNull Env that) {
        return plus(that.vars);
    }

    @Contract(pure = true)
    public @NotNull Env plus(final @NotNull @ReadOnly @Unmodifiable Map<@NotNull String, @NotNull String> thatVars) {
        final int thatSize = thatVars.size();
        if (thatSize == 0) {
            return this;
        }

        final Map<String, String> thisVars = this.vars;
        final int thisSize = thisVars.size();
        if (thisSize == 0) {
            return Env.envOf(thatVars);
        }

        final HashMap<String, String> vars = new HashMap<>(thisSize + thatSize, 1f);
        vars.putAll(thisVars);
        vars.putAll(thatVars);
        return new Env(unmodifiableMap(vars));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NotNull Env plus(final @NotNull String k, final @NotNull String v) {
        final Map<String, String> thisVars = this.vars;
        final int thisSize = thisVars.size();
        if (thisSize == 0) {
            return envOf(k, v);
        }

        final HashMap<String, String> vars = new HashMap<>(thisSize + 1, 1f);
        vars.putAll(thisVars);
        vars.put(k, v);
        return new Env(unmodifiableMap(vars));
    }

    @Contract(pure = true)
    public @NotNull Env minus(final @NotNull Env that) {
        return minus(that.vars);
    }

    @Contract(pure = true)
    public @NotNull Env minus(final @NotNull @ReadOnly @Unmodifiable Map<@NotNull String, ?> keys) {
        return minus(keys.keySet());
    }

    @Contract(pure = true)
    public @NotNull Env minus(final @NotNull @ReadOnly @Unmodifiable Collection<@NotNull String> keys) {
        final int keysSize = keys.size();
        if (keysSize == 0) {
            return this;
        }

        final Map<String, String> thisVars = this.vars;
        final HashMap<String, String> vars = new HashMap<>(thisVars.size() - keysSize, 1f);
        for (final Map.Entry<String, String> var : thisVars.entrySet()) {
            final String key = var.getKey();
            if (!keys.contains(key)) {
                vars.put(key, var.getValue());
            }
        }
        return new Env(unmodifiableMap(vars));
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Env minus(final @NotNull @ReadOnly @Unmodifiable Iterable<@NotNull String> keys) {
        final HashMap<String, String> vars = new HashMap<>(this.vars);
        for (final String key : keys) {
            vars.remove(key);
        }
        return new Env(unmodifiableMap(vars));
    }

    @Contract(pure = true)
    public @NotNull Env minus(final @NotNull @ReadOnly @Unmodifiable Iterator<@NotNull String> keys) {
        if (!keys.hasNext()) {
            return this;
        }

        final HashMap<String, String> vars = new HashMap<>(this.vars);
        while (keys.hasNext()) {
            vars.remove(keys.next());
        }
        return new Env(unmodifiableMap(vars));
    }

    @Contract(pure = true)
    public @NotNull Env minus(final @NotNull Stream<@NotNull String> keys) {
        final HashMap<String, String> vars = new HashMap<>(this.vars);
        keys.forEach(vars::remove);
        return new Env(unmodifiableMap(vars));
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "Env" + vars;
    }
}
