/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.jndi.internal.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * OSGiServiceNaming enumeration implementation..
 */
public class OSGiServiceNamingEnumeration implements NamingEnumeration<NameClassPair> {

    /**
     * Caller bundle context.
     */
    private BundleContext bundleContext;
    /**
     * Maintain current position of the references.
     */
    private int currentIndex;
    /**
     * Maintains list of bindings.
     */
    private List<NameClassPair> nameClassPairList;

    /**
     * create OSGiServiceNamingEnumeration instance building the NameClassPair objects.
     *
     * @param bundleContext owning bundle context
     * @param refs          servicereferences of each service of the registry
     */
    public OSGiServiceNamingEnumeration(BundleContext bundleContext, List<ServiceReference> refs) {
        this.bundleContext = bundleContext;
        nameClassPairList = buildNameClassPair(refs);
    }

    private List<NameClassPair> buildNameClassPair(List<ServiceReference> serviceReferencesList) {
        //A Binding object contains the name, class of the service, and the service object.
        //name are a string with the service.id number
        Predicate<ServiceReference> filterNotNullReferences =
                (ServiceReference reference) -> (bundleContext.getService(reference) != null);

        return serviceReferencesList.stream()
                .filter(filterNotNullReferences)
                .map(this::buildNameClassPair)
                .collect(Collectors.toList());
    }

    private NameClassPair buildNameClassPair(ServiceReference serviceReference) {
        NameClassPair nameClassPair =
                new NameClassPair(String.valueOf(serviceReference.getProperty(Constants.SERVICE_ID)),
                        bundleContext.getService(serviceReference).getClass().getName());
        bundleContext.ungetService(serviceReference);
        return nameClassPair;
    }

    /**
     * Retrieves the next element in the enumeration.
     */
    @Override
    public NameClassPair next() throws NamingException {
        return nextElement();
    }

    /**
     * Determines whether there are any more elements in the enumeration.
     */
    @Override
    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }

    @Override
    public void close() throws NamingException {
    }

    /**
     * Tests if this enumeration contains more elements.
     */
    @Override
    public boolean hasMoreElements() {
        return currentIndex < nameClassPairList.size();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     */
    @Override
    public NameClassPair nextElement() {
        return nameClassPairList.get(currentIndex++);
    }
}
