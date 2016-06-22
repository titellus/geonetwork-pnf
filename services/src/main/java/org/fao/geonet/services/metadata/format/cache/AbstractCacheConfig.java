/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.SystemInfo;

/**
 * Has the standard functionality for caching control.
 *
 * @author Jesse on 3/7/2015.
 */
public abstract class AbstractCacheConfig implements CacheConfig {
    @Override
    public final boolean allowCaching(Key key) {
        final SystemInfo systemInfo = ApplicationContextHolder.get().getBean(SystemInfo.class);
        final boolean isTesting = systemInfo == null;
        return (isTesting || !systemInfo.isDevMode()) && extraChecks(key);
    }

    /**
     * Perform extra checks to allow caching.  Return false to disallow caching
     */
    protected abstract boolean extraChecks(Key key);
}