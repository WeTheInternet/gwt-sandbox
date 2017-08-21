package com.google.gwt.dev;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.Transferable;
import com.google.gwt.dev.cfg.BindingProperty;
import com.google.gwt.dev.cfg.ConfigurationProperty;
import com.google.gwt.dev.js.JsNamespaceOption;

/**
 * A place to store various compilation properties / directories so that
 * end user code can look at these values when linking.
 *
 * We don't make the gwt compiler, by default, emit any public resource for this artifact,
 * and will leave it up to implementors to decide how and what information they want to expose.
 * (xapi project uses this to export a xapi-lang properties file for consumption by server runtime)
 *
 * Created by James X. Nelson (james @wetheinter.net) on 8/9/17.
 */
@Transferable
public class CompilePropertiesArtifact extends Artifact<CompilePropertiesArtifact> {

    private String genDir;
    private String workDir;
    private String warDir;
    private String extraDir;
    private String deployDir;
    private JsNamespaceOption jsNamespace;
    private String sourceMapPrefix;
    private List<String> moduleNames;
    private SortedSet<BindingProperty> bindingProps;
    private SortedSet<ConfigurationProperty> configProps;

    /**
     * @param linker the type of Linker that instantiated the Artifact.
     *
     *               By default, we will use Linker.class
     */
    protected CompilePropertiesArtifact(Class<? extends Linker> linker) {
        super(linker);
    }
    protected CompilePropertiesArtifact() {
        super(Linker.class);
    }

    @Override
    public int hashCode() {
        return moduleNames != null ? moduleNames.hashCode() : 0;
    }

    @Override
    protected int compareToComparableArtifact(CompilePropertiesArtifact o) {
        if (moduleNames == null) {
            if (o.moduleNames == null) {
                return 0;
            }
            return 1;
        } else if (o.moduleNames == null) {
            return -1;
        }
        final Iterator<String> mine = moduleNames.iterator();
        final Iterator<String> yours = o.moduleNames.iterator();
        while (mine.hasNext()) {
            if (!yours.hasNext()) {
                return -1;
            }
            int diff = mine.next().compareTo(yours.next());
            if (diff != 0) {
                return diff;
            }
        }
        if (yours.hasNext()) {
            return 1;
        }
        return 0;
    }

    @Override
    protected Class<CompilePropertiesArtifact> getComparableArtifactType() {
        return CompilePropertiesArtifact.class;
    }

    public void setGenDir(String genDir) {
        this.genDir = genDir;
    }

    public String getGenDir() {
        return genDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWarDir(String warDir) {
        this.warDir = warDir;
    }

    public String getWarDir() {
        return warDir;
    }

    public void setExtraDir(String extraDir) {
        this.extraDir = extraDir;
    }

    public String getExtraDir() {
        return extraDir;
    }

    public void setDeployDir(String deployDir) {
        this.deployDir = deployDir;
    }

    public String getDeployDir() {
        return deployDir;
    }

    public void setJsNamespace(JsNamespaceOption jsNamespace) {
        this.jsNamespace = jsNamespace;
    }

    public JsNamespaceOption getJsNamespace() {
        return jsNamespace;
    }

    public void setSourceMapPrefix(String sourceMapPrefix) {
        this.sourceMapPrefix = sourceMapPrefix;
    }

    public String getSourceMapPrefix() {
        return sourceMapPrefix;
    }

    public void setModuleNames(List<String> moduleNames) {
        this.moduleNames = moduleNames;
    }

    public List<String> getModuleNames() {
        return moduleNames;
    }

    public void setBindingProps(SortedSet<BindingProperty> bindingProps) {
        this.bindingProps = bindingProps;
    }

    public SortedSet<BindingProperty> getBindingProps() {
        return bindingProps;
    }

    public void setConfigProps(SortedSet<ConfigurationProperty> configProps) {
        this.configProps = configProps;
    }

    public SortedSet<ConfigurationProperty> getConfigProps() {
        return configProps;
    }
}
