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

package org.fao.geonet.api.tools.migration;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.api.API;
import org.fao.geonet.domain.Profile;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.sql.DataSource;
import java.sql.Connection;

@RequestMapping(value = {
        "/api/tools/migration",
        "/api/" + API.VERSION_0_1 +
                "/tools/migration"
})
@Api(value = "tools",
     tags= "tools",
     position = 100)
@Controller("migration")
public class MigrationApi {


    @ApiOperation(value = "Call a migration step",
                  nickname = "callStep")
    @RequestMapping(value = "/steps/{stepName}",
                    produces = MediaType.TEXT_PLAIN_VALUE,
                    method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<String> callStep(
            @ApiParam(value = "Class name to execute corresponding to a migration step. See DatabaseMigrationTask.",
                      example = "org.fao.geonet.api.records.attachments.MetadataResourceDatabaseMigration",
                      required = true)
            @PathVariable
            String stepName) throws Exception {
        Profile profile = ServiceContext.get().getUserSession().getProfile();
        if (profile != Profile.Administrator) {
            throw new SecurityException(String.format(
                    "Only administrator can run migration steps. Your profile is '%s'.",
                    profile == null ? "Anonymous" : profile
            ));
        }

        ApplicationContext appContext = ApplicationContextHolder.get();
        final DataSource dataSource = appContext.getBean(DataSource.class);
        try (Connection connection = dataSource.getConnection()) {
             DatabaseMigrationTask task =
                     (DatabaseMigrationTask) Class.forName(stepName).newInstance();
            task.update(connection);
            return new ResponseEntity<>("", HttpStatus.CREATED);
        } catch (ClassNotFoundException e) {
            return new ResponseEntity<>(String.format(
                    "Class '%s' not found. Choose a valid migration step.",
                    e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            String error = ex.getMessage();
            if (ex.getCause() != null)
                error = error + ". " + ex.getCause().getMessage();
            return new ResponseEntity<>(String.format(
                    "Error occurred during migration step '%s'. %s.",
                    stepName, error
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}