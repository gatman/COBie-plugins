package org.erdc.cobie.plugins.idm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.objectidms.ObjectIDM;
import org.bimserver.plugins.objectidms.ObjectIDMPlugin;
import org.bimserver.utils.CollectionUtils;
import org.erdc.cobie.shared.bimserver.utility.PluginRuntimeFileHelper;

public abstract class IDMPlugin implements ObjectIDMPlugin
{
	protected boolean initialized;
	protected COBieIDM cobieObjectIDM;
	protected File ignoreFile;
	private static final String FILE_BASED_OBJECT_IDM_PLUGIN_NAME = "FileBasedObjectIDMPlugin";

	public abstract String getIgnoreFilePath();

	@Override
	public ObjectIDM getObjectIDM(PluginConfiguration pluginConfiguration)
	{
		return cobieObjectIDM;
	}

	@Override
	public final ObjectDefinition getSettingsDefinition()
	{
		return null;
	}

	@Override
	public final String getVersion()
	{
		return "1.0";
	}

	@Override
	public void init(PluginManager pluginManager) throws PluginException
	{
		try
		{
			ignoreFile = PluginRuntimeFileHelper.prepareSerializerConfigFile(
					pluginManager, getDefaultName(), this, getIgnoreFilePath());
		}
		catch (FileNotFoundException e)
		{
			throw new PluginException("Could not find configuration files");
		}
		
		ObjectIDMPlugin idmPlugin = null;
		ArrayList<ObjectIDMPlugin> idmplugins = (ArrayList<ObjectIDMPlugin>) pluginManager
				.getAllObjectIDMPlugins(true);
		for (ObjectIDMPlugin plugin : idmplugins)
		{
			if (plugin.getDefaultName().equals(FILE_BASED_OBJECT_IDM_PLUGIN_NAME))
			{
				idmPlugin = plugin;
			}
		}
		
		cobieObjectIDM = new COBieIDM(ignoreFile,
				CollectionUtils.singleSet(Ifc2x3tc1Package.eINSTANCE),
				pluginManager.getPluginContext(this), idmPlugin);
		initialized = true;

	}

	@Override
	public boolean isInitialized()
	{
		return initialized;
	}
}
