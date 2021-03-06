/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.frameworkintegration.impl.felix;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkRunner;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;
import org.osmorc.run.ui.SelectedBundle;

import java.util.List;

/**
 * Felix specific implementation of {@link org.osmorc.frameworkintegration.FrameworkRunner}.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FelixRunner extends AbstractFrameworkRunner {
  static final String MAIN_CLASS = "org.apache.felix.main.Main";

  /**
   * See <a href="http://felix.apache.org/site/apache-felix-framework-configuration-properties.html">Felix Configuration Properties</a>.
   */
  @Override
  protected void setupParameters(@NotNull JavaParameters parameters) {
    ParametersList vmParameters = parameters.getVMParametersList();

    // bundles and start levels

    MultiMap<Integer, String> startBundles = new MultiMap<Integer, String>();
    List<String> installBundles = ContainerUtil.newSmartList();

    for (SelectedBundle bundle : myBundles) {
      String bundleUrl = bundle.getBundleUrl();
      if (bundleUrl == null) continue;
      boolean isFragment = CachingBundleInfoProvider.isFragmentBundle(bundleUrl);

      bundleUrl = bundleUrl.replaceAll(" ", "%20");
      if (bundle.isStartAfterInstallation() && !isFragment) {
        int startLevel = getBundleStartLevel(bundle);
        startBundles.putValue(startLevel, bundleUrl);
      }
      else {
        installBundles.add(bundleUrl);
      }
    }

    for (Integer startLevel : startBundles.keySet()) {
      vmParameters.addProperty("felix.auto.start." + startLevel, StringUtil.join(startBundles.get(startLevel), " "));
    }
    if (!installBundles.isEmpty()) {
      vmParameters.addProperty("felix.auto.install.1", StringUtil.join(installBundles, " "));
    }

    int startLevel = getFrameworkStartLevel();
    vmParameters.addProperty("org.osgi.framework.startlevel.beginning", String.valueOf(startLevel));

    int defaultStartLevel = myRunConfiguration.getDefaultStartLevel();
    vmParameters.addProperty("felix.startlevel.bundle", String.valueOf(defaultStartLevel));

    // framework-specific options

    vmParameters.addProperty("org.osgi.framework.storage.clean", "onFirstInit");

    if (GenericRunProperties.isDebugMode(myAdditionalProperties)) {
      vmParameters.addProperty("felix.log.level", "4");
    }

    parameters.setMainClass(MAIN_CLASS);
  }
}
