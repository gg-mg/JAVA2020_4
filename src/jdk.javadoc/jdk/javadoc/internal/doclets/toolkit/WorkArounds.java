/*
 * Copyright (c) 2015, 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.javadoc.internal.doclets.toolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.tools.doclint.DocLint;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Source.Feature;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Names;

import jdk.javadoc.internal.doclets.toolkit.util.Utils;
import jdk.javadoc.internal.tool.ToolEnvironment;
import jdk.javadoc.internal.tool.DocEnvImpl;

import static com.sun.tools.javac.code.Kinds.Kind.*;
import static com.sun.tools.javac.code.Scope.LookupKind.NON_RECURSIVE;

import static javax.lang.model.element.ElementKind.*;

/**
 * A quarantine class to isolate all the workarounds and bridges to
 * a locality. This class should eventually disappear once all the
 * standard APIs support the needed interfaces.
 *
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class WorkArounds {

    public final BaseConfiguration configuration;
    public final ToolEnvironment toolEnv;
    public final Utils utils;

    private DocLint doclint;

    public WorkArounds(BaseConfiguration configuration) {
        this.configuration = configuration;
        this.utils = this.configuration.utils;
        this.toolEnv = ((DocEnvImpl)this.configuration.docEnv).toolEnv;
    }

    Map<CompilationUnitTree, Boolean> shouldCheck = new HashMap<>();
    // TODO: fix this up correctly
    public void runDocLint(TreePath path) {
        CompilationUnitTree unit = path.getCompilationUnit();
        if (doclint != null && shouldCheck.computeIfAbsent(unit, doclint::shouldCheck)) {
            doclint.scan(path);
        }
    }

    // TODO: fix this up correctly
    public void initDocLint(Collection<String> opts, Collection<String> customTagNames) {
        ArrayList<String> doclintOpts = new ArrayList<>();
        boolean msgOptionSeen = false;

        for (String opt : opts) {
            if (opt.startsWith(DocLint.XMSGS_OPTION)) {
                if (opt.equals(DocLint.XMSGS_CUSTOM_PREFIX + "none"))
                    return;
                msgOptionSeen = true;
            }
            doclintOpts.add(opt);
        }

        if (!msgOptionSeen) {
            doclintOpts.add(DocLint.XMSGS_OPTION);
        }

        String sep = "";
        StringBuilder customTags = new StringBuilder();
        for (String customTag : customTagNames) {
            customTags.append(sep);
            customTags.append(customTag);
            sep = DocLint.SEPARATOR;
        }
        doclintOpts.add(DocLint.XCUSTOM_TAGS_PREFIX + customTags.toString());
        doclintOpts.add(DocLint.XHTML_VERSION_PREFIX + "html5");

        JavacTask t = BasicJavacTask.instance(toolEnv.context);
        doclint = new DocLint();
        doclint.init(t, doclintOpts.toArray(new String[doclintOpts.size()]), false);
    }

    // TODO: fix this up correctly
    public boolean haveDocLint() {
        return (doclint == null);
    }

    /*
     * TODO: This method exists because of a bug in javac which does not
     * handle "@deprecated tag in package-info.java", when this issue
     * is fixed this method and its uses must be jettisoned.
     */
    public boolean isDeprecated0(Element e) {
        if (!utils.getDeprecatedTrees(e).isEmpty()) {
            return true;
        }
        JavacTypes jctypes = ((DocEnvImpl)configuration.docEnv).toolEnv.typeutils;
        TypeMirror deprecatedType = utils.getDeprecatedType();
        for (AnnotationMirror anno : e.getAnnotationMirrors()) {
            if (jctypes.isSameType(anno.getAnnotationType().asElement().asType(), deprecatedType))
                return true;
        }
        return false;
    }

    // TODO: fix jx.l.m add this method.
    public boolean isSynthesized(AnnotationMirror aDesc) {
        return ((Attribute)aDesc).isSynthesized();
    }

    // TODO: fix the caller
    public Object getConstValue(VariableElement ve) {
        return ((VarSymbol)ve).getConstValue();
    }

    // TODO: DocTrees: Trees.getPath(Element e) is slow a factor 4-5 times.
    public Map<Element, TreePath> getElementToTreePath() {
        return toolEnv.elementToTreePath;
    }

    // TODO: we need ElementUtils.getPackage to cope with input strings
    // to return the proper unnamedPackage for all supported releases.
    PackageElement getUnnamedPackage() {
        return (Feature.MODULES.allowedInSource(toolEnv.source))
                ? toolEnv.syms.unnamedModule.unnamedPackage
                : toolEnv.syms.noModule.unnamedPackage;
    }

    // TODO: implement in either jx.l.m API (preferred) or DocletEnvironment.
    FileObject getJavaFileObject(PackageElement packageElement) {
        return ((PackageSymbol)packageElement).sourcefile;
    }

    // TODO: needs to ported to jx.l.m.
    public TypeElement searchClass(TypeElement klass, String className) {
        TypeElement te;

        // search by qualified name in current module first
        ModuleElement me = utils.containingModule(klass);
        if (me != null) {
            te = configuration.docEnv.getElementUtils().getTypeElement(me, className);
            if (te != null) {
                return te;
            }
        }

        // search inner classes
        for (TypeElement ite : utils.getClasses(klass)) {
            TypeElement innerClass = searchClass(ite, className);
            if (innerClass != null) {
                return innerClass;
            }
        }

        // check in this package
        te = utils.findClassInPackageElement(utils.containingPackage(klass), className);
        if (te != null) {
            return te;
        }

        ClassSymbol tsym = (ClassSymbol)klass;
        // make sure that this symbol has been completed
        // TODO: do we need this anymore ?
        if (tsym.completer != null) {
            tsym.complete();
        }

        // search imports
        if (tsym.sourcefile != null) {

            //### This information is available only for source classes.
            Env<AttrContext> compenv = toolEnv.getEnv(tsym);
            if (compenv == null) {
                return null;
            }
            Names names = tsym.name.table.names;
            Scope s = compenv.toplevel.namedImportScope;
            for (Symbol sym : s.getSymbolsByName(names.fromString(className))) {
                if (sym.kind == TYP) {
                    return (TypeElement)sym;
                }
            }

            s = compenv.toplevel.starImportScope;
            for (Symbol sym : s.getSymbolsByName(names.fromString(className))) {
                if (sym.kind == TYP) {
                    return (TypeElement)sym;
                }
            }
        }

        // finally, search by qualified name in all modules
        te = configuration.docEnv.getElementUtils().getTypeElement(className);
        if (te != null) {
            return te;
        }

        return null; // not found
    }

    // TODO:  need to re-implement this using j.l.m. correctly!, this has
    // implications on testInterface, the note here is that javac's supertype
    // does the right thing returning Parameters in scope.
    /**
     * Return the type containing the method that this method overrides.
     * It may be a <code>TypeElement</code> or a <code>TypeParameterElement</code>.
     * @param method target
     * @return a type
     */
    public TypeMirror overriddenType(ExecutableElement method) {
        if (utils.isStatic(method)) {
            return null;
        }
        MethodSymbol sym = (MethodSymbol)method;
        ClassSymbol origin = (ClassSymbol) sym.owner;
        for (com.sun.tools.javac.code.Type t = toolEnv.getTypes().supertype(origin.type);
                t.hasTag(TypeTag.CLASS);
                t = toolEnv.getTypes().supertype(t)) {
            ClassSymbol c = (ClassSymbol) t.tsym;
            for (com.sun.tools.javac.code.Symbol sym2 : c.members().getSymbolsByName(sym.name)) {
                if (sym.overrides(sym2, origin, toolEnv.getTypes(), true)) {
                    // Ignore those methods that may be a simple override
                    // and allow the real API method to be found.
                    if (sym2.type.hasTag(TypeTag.METHOD) &&
                            utils.isSimpleOverride((MethodSymbol)sym2)) {
                        continue;
                    }
                    return t;
                }
            }
        }
        return null;
    }

    // TODO: the method jx.l.m.Elements::overrides does not check
    // the return type, see JDK-8174840 until that is resolved,
    // use a  copy of the same method, with a return type check.

    // Note: the rider.overrides call in this method *must* be consistent
    // with the call in overrideType(....), the method above.
    public boolean overrides(ExecutableElement e1, ExecutableElement e2, TypeElement cls) {
        MethodSymbol rider = (MethodSymbol)e1;
        MethodSymbol ridee = (MethodSymbol)e2;
        ClassSymbol origin = (ClassSymbol)cls;

        return rider.name == ridee.name &&

               // not reflexive as per JLS
               rider != ridee &&

               // we don't care if ridee is static, though that wouldn't
               // compile
               !rider.isStatic() &&

               // Symbol.overrides assumes the following
               ridee.isMemberOf(origin, toolEnv.getTypes()) &&

               // check access, signatures and check return types
               rider.overrides(ridee, origin, toolEnv.getTypes(), true);
    }

    // TODO: jx.l.m ?
    public Location getLocationForModule(ModuleElement mdle) {
        ModuleSymbol msym = (ModuleSymbol)mdle;
        return msym.sourceLocation != null
                ? msym.sourceLocation
                : msym.classLocation;
    }

    //------------------Start of Serializable Implementation---------------------//
    private final static Map<TypeElement, NewSerializedForm> serializedForms = new HashMap<>();

    public SortedSet<VariableElement> getSerializableFields(Utils utils, TypeElement klass) {
        NewSerializedForm sf = serializedForms.get(klass);
        if (sf == null) {
            sf = new NewSerializedForm(utils, configuration.docEnv.getElementUtils(), klass);
            serializedForms.put(klass, sf);
        }
        return sf.fields;
    }

    public SortedSet<ExecutableElement>  getSerializationMethods(Utils utils, TypeElement klass) {
        NewSerializedForm sf = serializedForms.get(klass);
        if (sf == null) {
            sf = new NewSerializedForm(utils, configuration.docEnv.getElementUtils(), klass);
            serializedForms.put(klass, sf);
        }
        return sf.methods;
    }

    public boolean definesSerializableFields(Utils utils, TypeElement klass) {
        if (!utils.isSerializable(klass) || utils.isExternalizable(klass)) {
            return false;
        } else {
            NewSerializedForm sf = serializedForms.get(klass);
            if (sf == null) {
                sf = new NewSerializedForm(utils, configuration.docEnv.getElementUtils(), klass);
                serializedForms.put(klass, sf);
            }
            return sf.definesSerializableFields;
        }
    }

    /* TODO we need a clean port to jx.l.m
     * The serialized form is the specification of a class' serialization state.
     * <p>
     *
     * It consists of the following information:
     * <p>
     *
     * <pre>
     * 1. Whether class is Serializable or Externalizable.
     * 2. Javadoc for serialization methods.
     *    a. For Serializable, the optional readObject, writeObject,
     *       readResolve and writeReplace.
     *       serialData tag describes, in prose, the sequence and type
     *       of optional data written by writeObject.
     *    b. For Externalizable, writeExternal and readExternal.
     *       serialData tag describes, in prose, the sequence and type
     *       of optional data written by writeExternal.
     * 3. Javadoc for serialization data layout.
     *    a. For Serializable, the name,type and description
     *       of each Serializable fields.
     *    b. For Externalizable, data layout is described by 2(b).
     * </pre>
     *
     */
    static class NewSerializedForm {

        final Utils utils;
        final Elements elements;

        final SortedSet<ExecutableElement> methods;

        /* List of FieldDocImpl - Serializable fields.
         * Singleton list if class defines Serializable fields explicitly.
         * Otherwise, list of default serializable fields.
         * 0 length list for Externalizable.
         */
        final SortedSet<VariableElement> fields;

        /* True if class specifies serializable fields explicitly.
         * using special static member, serialPersistentFields.
         */
        boolean definesSerializableFields = false;

        // Specially treated field/method names defined by Serialization.
        private static final String SERIALIZABLE_FIELDS = "serialPersistentFields";
        private static final String READOBJECT = "readObject";
        private static final String WRITEOBJECT = "writeObject";
        private static final String READRESOLVE = "readResolve";
        private static final String WRITEREPLACE = "writeReplace";
        private static final String READOBJECTNODATA = "readObjectNoData";

        NewSerializedForm(Utils utils, Elements elements, TypeElement te) {
            this.utils = utils;
            this.elements = elements;
            methods = new TreeSet<>(utils.makeGeneralPurposeComparator());
            fields = new TreeSet<>(utils.makeGeneralPurposeComparator());
            if (utils.isExternalizable(te)) {
                /* look up required public accessible methods,
                 *   writeExternal and readExternal.
                 */
                String[] readExternalParamArr = {"java.io.ObjectInput"};
                String[] writeExternalParamArr = {"java.io.ObjectOutput"};

                ExecutableElement md = findMethod(te, "readExternal", Arrays.asList(readExternalParamArr));
                if (md != null) {
                    methods.add(md);
                }
                md = findMethod((ClassSymbol) te, "writeExternal", Arrays.asList(writeExternalParamArr));
                if (md != null) {
                    methods.add(md);
                }
            } else if (utils.isSerializable(te)) {
                VarSymbol dsf = getDefinedSerializableFields((ClassSymbol) te);
                if (dsf != null) {
                    /* Define serializable fields with array of ObjectStreamField.
                     * Each ObjectStreamField should be documented by a
                     * serialField tag.
                     */
                    definesSerializableFields = true;
                    fields.add((VariableElement) dsf);
                } else {

                    /* Calculate default Serializable fields as all
                     * non-transient, non-static fields.
                     * Fields should be documented by serial tag.
                     */
                    computeDefaultSerializableFields((ClassSymbol) te);
                }

                /* Check for optional customized readObject, writeObject,
                 * readResolve and writeReplace, which can all contain
                 * the serialData tag.        */
                addMethodIfExist((ClassSymbol) te, READOBJECT);
                addMethodIfExist((ClassSymbol) te, WRITEOBJECT);
                addMethodIfExist((ClassSymbol) te, READRESOLVE);
                addMethodIfExist((ClassSymbol) te, WRITEREPLACE);
                addMethodIfExist((ClassSymbol) te, READOBJECTNODATA);
            }
        }

        private VarSymbol getDefinedSerializableFields(ClassSymbol def) {
            Names names = def.name.table.names;

            /* SERIALIZABLE_FIELDS can be private,
             */
            for (Symbol sym : def.members().getSymbolsByName(names.fromString(SERIALIZABLE_FIELDS))) {
                if (sym.kind == VAR) {
                    VarSymbol f = (VarSymbol) sym;
                    if ((f.flags() & Flags.STATIC) != 0
                            && (f.flags() & Flags.PRIVATE) != 0) {
                        return f;
                    }
                }
            }
            return null;
        }

        /*
         * Catalog Serializable method if it exists in current ClassSymbol.
         * Do not look for method in superclasses.
         *
         * Serialization requires these methods to be non-static.
         *
         * @param method should be an unqualified Serializable method
         *               name either READOBJECT, WRITEOBJECT, READRESOLVE
         *               or WRITEREPLACE.
         * @param visibility the visibility flag for the given method.
         */
        private void addMethodIfExist(ClassSymbol def, String methodName) {
            Names names = def.name.table.names;

            for (Symbol sym : def.members().getSymbolsByName(names.fromString(methodName))) {
                if (sym.kind == MTH) {
                    MethodSymbol md = (MethodSymbol) sym;
                    if ((md.flags() & Flags.STATIC) == 0) {
                        /*
                         * WARNING: not robust if unqualifiedMethodName is overloaded
                         *          method. Signature checking could make more robust.
                         * READOBJECT takes a single parameter, java.io.ObjectInputStream.
                         * WRITEOBJECT takes a single parameter, java.io.ObjectOutputStream.
                         */
                        methods.add(md);
                    }
                }
            }
        }

        /*
         * Compute default Serializable fields from all members of ClassSymbol.
         *
         * must walk over all members of ClassSymbol.
         */
        private void computeDefaultSerializableFields(ClassSymbol te) {
            for (Symbol sym : te.members().getSymbols(NON_RECURSIVE)) {
                if (sym != null && sym.kind == VAR) {
                    VarSymbol f = (VarSymbol) sym;
                    if ((f.flags() & Flags.STATIC) == 0
                            && (f.flags() & Flags.TRANSIENT) == 0) {
                        //### No modifier filtering applied here.
                        //### Add to beginning.
                        //### Preserve order used by old 'javadoc'.
                        fields.add(f);
                    }
                }
            }
        }

        /**
         * Find a method in this class scope. Search order: this class, interfaces, superclasses,
         * outerclasses. Note that this is not necessarily what the compiler would do!
         *
         * @param methodName the unqualified name to search for.
         * @param paramTypes the array of Strings for method parameter types.
         * @return the first MethodDocImpl which matches, null if not found.
         */
        public ExecutableElement findMethod(TypeElement te, String methodName,
                List<String> paramTypes) {
            List<? extends Element> allMembers = this.elements.getAllMembers(te);
            loop:
            for (Element e : allMembers) {
                if (e.getKind() != METHOD) {
                    continue;
                }
                ExecutableElement ee = (ExecutableElement) e;
                if (!ee.getSimpleName().contentEquals(methodName)) {
                    continue;
                }
                List<? extends VariableElement> parameters = ee.getParameters();
                if (paramTypes.size() != parameters.size()) {
                    continue;
                }
                for (int i = 0; i < parameters.size(); i++) {
                    VariableElement ve = parameters.get(i);
                    if (!ve.asType().toString().equals(paramTypes.get(i))) {
                        break loop;
                    }
                }
                return ee;
            }
            TypeElement encl = utils.getEnclosingTypeElement(te);
            if (encl == null) {
                return null;
            }
            return findMethod(encl, methodName, paramTypes);
        }
    }

    // TODO: we need to eliminate this, as it is hacky.
    /**
     * Returns a representation of the package truncated to two levels.
     * For instance if the given package represents foo.bar.baz will return
     * a representation of foo.bar
     * @param pkg the PackageElement
     * @return an abbreviated PackageElement
     */
    public PackageElement getAbbreviatedPackageElement(PackageElement pkg) {
        String parsedPackageName = utils.parsePackageName(pkg);
        ModuleElement encl = (ModuleElement) pkg.getEnclosingElement();
        PackageElement abbrevPkg = encl == null
                ? utils.elementUtils.getPackageElement(parsedPackageName)
                : ((JavacElements) utils.elementUtils).getPackageElement(encl, parsedPackageName);
        return abbrevPkg;
    }
}
