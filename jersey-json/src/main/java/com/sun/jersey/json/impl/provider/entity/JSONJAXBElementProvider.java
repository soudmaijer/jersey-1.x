/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.json.impl.provider.entity;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.core.provider.jaxb.AbstractJAXBElementProvider;
import com.sun.jersey.core.util.FeaturesAndProperties;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 *
 * @author japod
 */
public class JSONJAXBElementProvider extends AbstractJAXBElementProvider {
    
    boolean jacksonEntityProviderTakesPrecedence = false;

    JSONJAXBElementProvider(Providers ps) {
        super(ps);
    }

    JSONJAXBElementProvider(Providers ps, MediaType mt) {
        super(ps, mt);
    }

    @Context @Override
    public void setConfiguration(FeaturesAndProperties fp) {
        super.setConfiguration(fp);
        jacksonEntityProviderTakesPrecedence = fp.getFeature(JSONConfiguration.FEATURE_POJO_MAPPING);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return !jacksonEntityProviderTakesPrecedence && super.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return !jacksonEntityProviderTakesPrecedence && super.isWriteable(type, genericType, annotations, mediaType);
    }

    @Produces("application/json")
    @Consumes("application/json")
    public static final class App extends JSONJAXBElementProvider {
        public App(@Context Providers ps) { super(ps , MediaType.APPLICATION_JSON_TYPE); }
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JSONJAXBElementProvider {
        public General(@Context Providers ps) { super(ps); }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+json");
        }
    }

    @Override
    protected final JAXBElement<?> readFrom(Class<?> type, MediaType mediaType,
            Unmarshaller u, InputStream entityStream)
            throws JAXBException {
        final Charset c = getCharset(mediaType);

        return JSONJAXBContext.getJSONUnmarshaller(u).
                unmarshalJAXBElementFromJSON(new InputStreamReader(entityStream, c), type);
    }
    
    @Override
    protected final void writeTo(JAXBElement<?> t, MediaType mediaType, Charset c,
            Marshaller m, OutputStream entityStream)
            throws JAXBException {

        JSONMarshaller jsonMarshaller = JSONJAXBContext.getJSONMarshaller(m);
        if(isFormattedOutput())
            jsonMarshaller.setProperty(JSONMarshaller.FORMATTED, true);
        jsonMarshaller.marshallToJSON(t, new OutputStreamWriter(entityStream, c));
    }
}