//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.setting;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.Setting_;
import org.fao.geonet.repository.SettingRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

/**
 * A convenience class for updating and accessing settings.  One of the primary needs of this
 * class at the moment is to maintain backwards compatibility so not all code and xsl files
 * that make use of the settings need to be modified.
 */
public class SettingManager {

    @Autowired
    private SettingRepository _repo;

    @PersistenceContext
    private EntityManager _entityManager;

    /**
     * Get all settings as xml.
     *
     * @param asTree get the settings as a tree
     *
     * @return all settings as xml.
     */
    public Element getAllAsXML(boolean asTree) {
        Element env = new Element("settings");
        List<Setting> settings = _repo.findAll(new Sort(Setting_.name.getName()));

        Map<String, Element> pathElements = new HashMap<String, Element>();

        for (Setting setting : settings) {
            if (asTree) {
                buildXmlTree(env, pathElements, setting);
            } else {
                Element settingEl = new Element("setting");
                settingEl.setAttribute("name", setting.getName());
                settingEl.setAttribute("position", String.valueOf(setting.getPosition()));
                settingEl.setAttribute("datatype", String.valueOf(setting.getDataType()));
                settingEl.setText(setting.getValue());
                env.addContent(settingEl);
            }
        }
        return env;
    }

    private void buildXmlTree(Element env, Map<String, Element> pathElements, Setting setting) {
        String[] segments = setting.getName().split("/");
        Element parent = env;
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            Element currentElement = pathElements.get(segment);
            if (currentElement == null) {
                currentElement = new Element(segment);
                currentElement.setAttribute("position", String.valueOf(setting.getPosition()));
                if (i == segments.length - 1) {
                    currentElement.setAttribute("datatype", String.valueOf(setting.getDataType().ordinal()));
                    currentElement.setAttribute("datatypeName", setting.getDataType().name());
                    currentElement.setText(setting.getValue());
                }
                parent.addContent(currentElement);
                pathElements.put(segment, currentElement);
            }

            parent = currentElement;
        }
    }

    /**
     * Return a setting by its key
     * 
     * @param path eg. system/site/name
     */
    public String getValue(String path) {
        if (Log.isDebugEnabled(Geonet.SETTINGS)) {
            Log.debug(Geonet.SETTINGS, "Requested setting with name: " + path);
        }
        Setting se = _repo.findOne(path);
        if (se == null) {
            // TODO : When a settings is not available in the settings table
            // we end here. It could be relevant to add a list of default
            // settings and populate the settings table when the settings is
            // missing (due to bad migration for example).
            Log.error(Geonet.SETTINGS, "  Requested setting with name: " + path + "  not found. Add it to the settings table.");
            return null;
        }
        String value = se.getValue();
        if (value == null) {
            Log.warning(Geonet.SETTINGS, "  Requested setting with name: " + path + " but null value found. Check the settings table.");
        }
        return value;
    }

    /**
     * Return a set of values as XML
     * 
     * @param keys A list of setting's key to retrieve
     */
    public Element getValues(String[] keys) {
        Element env = new Element("settings");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Setting se = _repo.findOne(key);
            if (se == null) {
                Log.error(Geonet.SETTINGS, "  Requested setting with name: " + key + " not found. Add it to the settings table.");
            } else {
                String value = se.getValue();
                if (value != null) {
                    Element setting = new Element("setting");
                    setting.setAttribute("name", key).setAttribute("value", value);
                    env.addContent(setting);
                }
            }
        }
        return env;
    }

    /**
     * Get value of a setting as boolean
     * 
     * @param key The setting key
     * @return The setting valueThe setting key
     */
    public boolean getValueAsBool(String key) {
        String value = getValue(key);
        if (value == null)
            return false;
        return Boolean.parseBoolean(value);
    }

    /**
     * Get value of a setting as boolean
     * 
     * @param key The setting key
     * @param defaultValue The default value
     * @return The setting value as boolean
     */
    public boolean getValueAsBool(String key, boolean defaultValue) {
        String value = getValue(key);
        return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Get value of a setting as integer
     * 
     * @param key The setting key
     * @return The integer value of the setting or null
     */
    public Integer getValueAsInt(String key) {
        String value = getValue(key);
        if (value == null || value.trim().length() == 0)
            return null;
        return Integer.valueOf(value);
    }

    /**
     * Set the value of a Setting entity
     * 
     * @param key the path/name/key of the setting.
     * @param value the new value
     * 
     * @return true if the types are correct and the setting is found.
     */
    public boolean setValue(Dbms dbms, String key, String value) throws Exception {
        if (Log.isDebugEnabled(Geonet.SETTINGS)) {
            Log.debug(Geonet.SETTINGS, "Setting with name: " + key + ", value: " + value);
        }

        Setting setting = _repo.findOne(key);

        if (setting == null) {
            throw new NoSuchElementException("There is no existing setting element with the key: " + key);
        }

        setting.getDataType().validate(value);

        setting.setValue(value);

        _repo.save(setting);
        return true;
    }

    /**
     * Set the setting value by key to the boolean value.
     * 
     * @param key the key/path/name of the setting.
     * @param value the new boolean value
     */
    public boolean setValue(Dbms dbms, String key, boolean value) throws Exception {
        return setValue(dbms, key, String.valueOf(value));
    }

    /**
     * Set a list of settings.
     * 
     * @param dbms
     * @param values The settings to update
     * @return true if the types are correct and the setting is found.
     * 
     * @throws SQLException
     */
    public boolean setValues(Dbms dbms, Map<String, String> values) throws Exception {
        boolean success = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            try {
                setValue(dbms, key, value);
            } catch (Exception e) {
                Log.warning(Geonet.SETTINGS,
                        "Failed to save setting with name: " + key + ", value: " + value + ". Error: " + e.getMessage());
                throw e;
            }
        }
        return success;
    }

    /**
     * Refreshes current settings manager. This has to be used when updating the Settings table without using this class. For example when
     * using an SQL script.
     */
    public boolean refresh() throws SQLException {
        _entityManager.getEntityManagerFactory().getCache().evict(HarvesterSetting.class);
        return true;
    }

    public String getSiteId() {
        return getValue("system/site/siteId");
    }

    public String getSiteName() {
        return getValue("system/site/name");
    }
}