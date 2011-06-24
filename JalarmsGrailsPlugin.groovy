/*
 * grails-jalarms: JAlarms Grails plugin
 * Copyright 2010 and beyond, Hackergarten
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *     
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
 */

import com.solab.alarms.AlarmChannel
import com.solab.alarms.AlarmSender
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * @author Hackergarten
 */
class JalarmsGrailsPlugin {
    def groupId = 'com.solab.grails'
    // the plugin version
    def version = "0.2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.5 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Enrique Zamudio, Hector Cuesta, Andres Almiray"
    def authorEmail = "chochos@users.sourceforge.net, aalmiray@users.sourceforge.net"
    def title = "JAlarms plugin for Grails"
    def description = '''\\
This plugin enables Grails applications to send alarms with a multitude of configurable channels.
Relies on JAlarms http://jalarms.sourceforge.net
'''

    def observe = ['controllers', 'services']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/jalarms"

    def doWithDynamicMethods = { ctx ->
        processArtifacts()
    }

    def onChange = { event ->
        processArtifacts()
    }

    def onConfigChange = { event ->
        processArtifacts()
    }

    private processArtifacts() {
        def config = ConfigurationHolder.config
        def application = ApplicationHolder.application
        def types = config.grails?.jalarms?.injectInto ?: ["Controller", "Service"]
        types.each { type ->
            application.getArtefacts(type).each{ klass ->
                klass.metaClass.sendAlarm = sendAlarm
            }
        }
    }
    
    private sendAlarm = {String... args ->
        ApplicationHolder.application.mainContext.alarmer.sendAlarm(*args)
    }

    def doWithSpring = {
        alarmer(AlarmSender) { bean ->
            bean.scope = 'singleton'
            bean.autowire = 'byName'
        }
    }
    
    def doWithApplicationContext = { applicationContext ->
        Map<String, AlarmChannel> channels = applicationContext.getBeansOfType(AlarmChannel)

        if(channels) {
            List<AlarmChannel> chnls = []
            chnls.addAll(channels.values())
            applicationContext.alarmer.alarmChannels = chnls
        }
    }
}
