package org.erdc.cobie.plugins.deserializers;

/******************************************************************************
 * Copyright (C) 2011  ERDC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
import java.io.File;
import java.io.FileNotFoundException;

import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.deserializers.Deserializer;
import org.bimserver.plugins.deserializers.DeserializerPlugin;
import org.bimserver.plugins.schema.SchemaException;
import org.erdc.cobie.shared.bimserver.deserialization.COBieDeserializer;
import org.erdc.cobie.shared.bimserver.deserialization.COBieDeserializerPluginName;
import org.erdc.cobie.shared.bimserver.utility.PluginRuntimeFileHelper;

public class COBieDeserializerPlugin implements DeserializerPlugin
{
	private static String localConfigFilePath = "lib/COBieExcelTemplate.xml";
	private boolean initialized = false;
	private File configurationFile;

	@Override
	public boolean canHandleExtension(String extension)
	{
		return extension.equalsIgnoreCase("xml");
	}

	@Override
	public Deserializer createDeserializer(
			PluginConfiguration pluginConfiguration)
	{
		return new COBieDeserializer(configurationFile);
	}

	@Override
	public String getDefaultName()
	{
		return COBieDeserializerPluginName.COBIE_SPREADHSEET.toString();
	}

	@Override
	public String getDescription()
	{
		return "Deserializes COBie spreadsheetML into the model server.";
	}

	@Override
	public ObjectDefinition getSettingsDefinition()
	{
		return null;
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}

	@Override
	public void init(PluginManager pluginManager) throws SchemaException,
			PluginException
	{
		try
		{
			configurationFile = PluginRuntimeFileHelper
					.prepareSerializerConfigFile(pluginManager,
							"COBieDeserializer", this, localConfigFilePath);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			throw new PluginException("Could not find configuration files");
		}
		initialized = true;
	}

	@Override
	public boolean isInitialized()
	{
		return initialized;
	}
}