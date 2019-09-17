package org.duke.cmd;

import org.duke.Duke;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Handler {

    private static final Map<Class<? extends Handler>, String> descriptionCache = new HashMap<>();
    private static final Map<Class<? extends Handler>, String> syntaxCache = new HashMap<>();

    public boolean handle(Duke duke, Command command) {
        handleNoExit(duke, command);
        return false;
    }

    protected void handleNoExit(Duke duke, Command command) {
    }

    public final String getDescriptionLine() {
        return descriptionCache.computeIfAbsent(this.getClass(),
                clazz -> {
                    StringBuilder firstLine = new StringBuilder();
                    for (Binding bind : this.getBindings()) {
                        firstLine.append(bind.value()).append(' ');
                    }
                    firstLine.append("- ").append(this.getDescriptionText().value());
                    return firstLine.toString();
                }
                );
    }

    public final String getSyntaxLine() {
        return syntaxCache.computeIfAbsent(this.getClass(),
                clazz -> {
                    String primaryBind = this.getPrimaryBinding();
                    StringBuilder secondLine = new StringBuilder(primaryBind);

                    String argumentDesc = this.getDescriptionText().argument();
                    if (!argumentDesc.isEmpty()) {
                        secondLine.append(" <").append(argumentDesc).append('>');
                    }

                    for (NamedArgument arg : this.getNamedArguments()) {
                        secondLine.append(" [ /")
                                .append(arg.value())
                                .append(" (")
                                .append(arg.description())
                                .append(") ]");
                    }
                    return secondLine.toString();
                });
    }

    public final String getPrimaryBinding() {
        return this.getBindings()[0].value();
    }

    private Binding[] getBindings() {
        return this.getClass().getAnnotationsByType(Binding.class);
    }

    private Description getDescriptionText()  {
        return this.getClass().getAnnotation(Description.class);
    }

    private NamedArgument[] getNamedArguments() {
        return this.getClass().getAnnotationsByType(NamedArgument.class);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Repeatable(Bindings.class)
    public @interface Binding {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Bindings {
        Binding[] value();
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Description {
        String value();
        String argument() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Repeatable(NamedArguments.class)
    public @interface NamedArgument {
        String value();
        String description();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface NamedArguments {
        NamedArgument[] value();
    }
}
