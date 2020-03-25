/*
 * Copyright (c) 2003, 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.javadoc.internal.doclets.toolkit.util.links;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor9;

import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;

/**
 * A factory that constructs links from given link information.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @author Jamie Ho
 */
public abstract class LinkFactory {
    protected final Utils utils;

    protected LinkFactory(Utils utils) {
        this.utils = utils;
    }

    /**
     * Return an empty instance of a content object.
     *
     * @return an empty instance of a content object.
     */
    protected abstract Content newContent();

    /**
     * Constructs a link from the given link information.
     *
     * @param linkInfo the information about the link.
     * @return the output of the link.
     */
    public Content getLink(LinkInfo linkInfo) {
        if (linkInfo.type != null) {
            SimpleTypeVisitor9<Content, LinkInfo> linkVisitor =
                    new SimpleTypeVisitor9<Content, LinkInfo>() {

                TypeMirror componentType = utils.getComponentType(linkInfo.type);
                Content link = newContent();

                // handles primitives, no types and error types
                @Override
                protected Content defaultAction(TypeMirror type, LinkInfo linkInfo) {
                    link.add(utils.getTypeName(type, false));
                    return link;
                }

                int currentDepth = 0;
                @Override
                public Content visitArray(ArrayType type, LinkInfo linkInfo) {
                    // keep track of the dimension depth and replace the last dimension
                    // specifier with vararags, when the stack is fully unwound.
                    currentDepth++;
                    linkInfo.type = type.getComponentType();
                    visit(linkInfo.type, linkInfo);
                    currentDepth--;
                    if (utils.isAnnotated(type)) {
                        linkInfo.type = type;
                        link.add(" ");
                        link.add(getTypeAnnotationLinks(linkInfo));
                    }
                    // use vararg if required
                    if (linkInfo.isVarArg && currentDepth == 0) {
                        link.add("...");
                    } else {
                        link.add("[]");
                    }
                    return link;
                }

                @Override
                public Content visitWildcard(WildcardType type, LinkInfo linkInfo) {
                    linkInfo.isTypeBound = true;
                    link.add("?");
                    TypeMirror extendsBound = type.getExtendsBound();
                    if (extendsBound != null) {
                        link.add(" extends ");
                        setBoundsLinkInfo(linkInfo, extendsBound);
                        link.add(getLink(linkInfo));
                    }
                    TypeMirror superBound = type.getSuperBound();
                    if (superBound != null) {
                        link.add(" super ");
                        setBoundsLinkInfo(linkInfo, superBound);
                        link.add(getLink(linkInfo));
                    }
                    return link;
                }

                @Override
                public Content visitTypeVariable(TypeVariable type, LinkInfo linkInfo) {
                    link.add(getTypeAnnotationLinks(linkInfo));
                    linkInfo.isTypeBound = true;
                    TypeVariable typevariable = (utils.isArrayType(type))
                            ? (TypeVariable) componentType
                            : type;
                    Element owner = typevariable.asElement().getEnclosingElement();
                    if ((!linkInfo.excludeTypeParameterLinks) && utils.isTypeElement(owner)) {
                        linkInfo.typeElement = (TypeElement) owner;
                        Content label = newContent();
                        label.add(utils.getTypeName(type, false));
                        linkInfo.label = label;
                        link.add(getClassLink(linkInfo));
                    } else {
                        // No need to link method type parameters.
                        link.add(utils.getTypeName(typevariable, false));
                    }

                    if (!linkInfo.excludeTypeBounds) {
                        linkInfo.excludeTypeBounds = true;
                        TypeParameterElement tpe = ((TypeParameterElement) typevariable.asElement());
                        boolean more = false;
                        List<? extends TypeMirror> bounds = utils.getBounds(tpe);
                        for (TypeMirror bound : bounds) {
                            // we get everything as extends java.lang.Object we suppress
                            // all of them except those that have multiple extends
                            if (bounds.size() == 1 &&
                                    bound.equals(utils.getObjectType()) &&
                                    !utils.isAnnotated(bound)) {
                                continue;
                            }
                            link.add(more ? " & " : " extends ");
                            setBoundsLinkInfo(linkInfo, bound);
                            link.add(getLink(linkInfo));
                            more = true;
                        }
                    }
                    return link;
                }

                @Override
                public Content visitDeclared(DeclaredType type, LinkInfo linkInfo) {
                    if (linkInfo.isTypeBound && linkInfo.excludeTypeBoundsLinks) {
                        // Since we are excluding type parameter links, we should not
                        // be linking to the type bound.
                        link.add(utils.getTypeName(type, false));
                        link.add(getTypeParameterLinks(linkInfo));
                        return link;
                    } else {
                        link = newContent();
                        link.add(getTypeAnnotationLinks(linkInfo));
                        linkInfo.typeElement = utils.asTypeElement(type);
                        link.add(getClassLink(linkInfo));
                        if (linkInfo.includeTypeAsSepLink) {
                            link.add(getTypeParameterLinks(linkInfo, false));
                        }
                    }
                    return link;
                }
            };
            return linkVisitor.visit(linkInfo.type, linkInfo);
        } else if (linkInfo.typeElement != null) {
            Content link = newContent();
            link.add(getClassLink(linkInfo));
            if (linkInfo.includeTypeAsSepLink) {
                link.add(getTypeParameterLinks(linkInfo, false));
            }
            return link;
        } else {
            return null;
        }
    }

    private void setBoundsLinkInfo(LinkInfo linkInfo, TypeMirror bound) {
        linkInfo.typeElement = null;
        linkInfo.label = null;
        linkInfo.type = bound;
    }

    /**
     * Returns a link to the given class.
     *
     * @param linkInfo the information about the link to construct
     *
     * @return the link for the given class.
     */
    protected abstract Content getClassLink(LinkInfo linkInfo);

    /**
     * Returns links to the type parameters.
     *
     * @param linkInfo     the information about the link to construct
     * @param isClassLabel true if this is a class label, or false if it is
     *                     the type parameters portion of the link
     * @return the links to the type parameters
     */
    protected abstract Content getTypeParameterLinks(LinkInfo linkInfo, boolean isClassLabel);

    /**
     * Returns links to the type parameters.
     *
     * @param linkInfo     the information about the link to construct
     * @return the links to the type parameters.
     */
    public Content getTypeParameterLinks(LinkInfo linkInfo) {
        return getTypeParameterLinks(linkInfo, true);
    }

    public abstract Content getTypeAnnotationLinks(LinkInfo linkInfo);
}
