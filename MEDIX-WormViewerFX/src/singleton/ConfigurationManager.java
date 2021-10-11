
package singleton;

import object.Configuration;
import object.DPConfiguration;
import object.MFConfiguration;
import object.DVConfiguration;
import object.GTConfiguration;

/**
 *
 * @author mshao1
 */


public class ConfigurationManager {
    private static ConfigurationManager configurationManager =  null;
    
    private Configuration configuration = null;
    private DVConfiguration dvConfiguration = null;
    private MFConfiguration mfConfiguration = null;
    private GTConfiguration gtConfiguration = null;
    private DPConfiguration dpConfiguration = null;
    
    static {
        configurationManager = new ConfigurationManager();
    }
    
    private ConfigurationManager() {
        configuration =  new Configuration();
        dvConfiguration = new DVConfiguration();
        mfConfiguration = new MFConfiguration();
        gtConfiguration = new GTConfiguration();
        dpConfiguration = new DPConfiguration();
    }
    
    public final static ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
    
    public final Configuration getConfiguration() {
        return configuration;
    }
    
    public final DVConfiguration getDVConfiguration() {
        return dvConfiguration;
    }

    public final GTConfiguration getGTConfiguration() {
        return gtConfiguration;
    }
    
    public final MFConfiguration getMFConfiguration() {
        return mfConfiguration;
    }
    
    public final DPConfiguration getDPConfiguration() {
        return dpConfiguration;
    }
}
