/**
 *  Copyright 2018 Vahur Kiisler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  JasonCoon FastLed WebServer
 *
 *  Author: Vahur Kiisler (vkiisler)
 *  Date: 2018-10-16
 *
 * Instructions:
 * Follow instructions on https://github.com/jasoncoon/esp8266-fastled-webserver
 *
 * Configure fastled webserver to use static IP
 *
 * Install this device handler, add device 
 *
 * Optional: open http://<ip of fastled webserver>/all
 *           copy response body to getInitProperties between ''' marks
 */

def getInitProperties() {
	final deviceDefaultProperties = 

	'''
	[{"name":"power","label":"Power","type":"Boolean","value":1},{"name":"brightness","label":"Brightness","type":"Number","value":255,"min":1,"max":255},{"name":"pattern","label":"Pattern","type":"Select","value":1,"options":["Pride","Color Waves","Rainbow Twinkles","Snow Twinkles","Cloud Twinkles","Incandescent Twinkles","Retro C9 Twinkles","Red & White Twinkles","Blue & White Twinkles","Red, Green & White Twinkles","Fairy Light Twinkles","Snow 2 Twinkles","Holly Twinkles","Ice Twinkles","Party Twinkles","Forest Twinkles","Lava Twinkles","Fire Twinkles","Cloud 2 Twinkles","Ocean Twinkles","Rainbow","Rainbow With Glitter","Solid Rainbow","Confetti","Sinelon","Beat","Juggle","Fire","Water","Solid Color"]},{"name":"palette","label":"Palette","type":"Select","value":0,"options":["Rainbow","Rainbow Stripe","Cloud","Lava","Ocean","Forest","Party","Heat"]},{"name":"speed","label":"Speed","type":"Number","value":30,"min":1,"max":255},{"name":"autoplay","label":"Autoplay","type":"Section"},{"name":"autoplay","label":"Autoplay","type":"Boolean","value":0},{"name":"autoplayDuration","label":"Autoplay Duration","type":"Number","value":1,"min":0,"max":255},{"name":"solidColor","label":"Solid Color","type":"Section"},{"name":"solidColor","label":"Color","type":"Color","value":"0,128,255"},{"name":"fire","label":"Fire & Water","type":"Section"},{"name":"cooling","label":"Cooling","type":"Number","value":49,"min":0,"max":255},{"name":"sparking","label":"Sparking","type":"Number","value":60,"min":0,"max":255},{"name":"twinkles","label":"Twinkles","type":"Section"},{"name":"twinkleSpeed","label":"Twinkle Speed","type":"Number","value":4,"min":0,"max":8},{"name":"twinkleDensity","label":"Twinkle Density","type":"Number","value":5,"min":0,"max":8}]
	'''
	return new groovy.json.JsonSlurper().parseText(deviceDefaultProperties)
}

preferences {
	section("Effcts"){
		generateSetupSelector("pattern", "Pattern")
		generateSetupSelector("palette", "Palette")
	}
	section("Internal Access"){
		input "internal_ip", "text", title: "Internal IP", required: true
		input "internal_port", "text", title: "Internal Port (if not 80)", required: false
	}
	input "logLevel", "number", title: "Log level", required: false
}

metadata {
	definition (name: "JasonCoon Fastled WebServer", namespace: "vkiisler", author: "Vahur Kiisler") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Color Control"
		capability "Polling"
		capability "Refresh"

		command autoOn
		command autoOff
		command setSpeed, ["number"]
		command setCooling, ["number"]
		command setSparking, ["number"]
		command setTwinkleSpeed, ["number"]
		command setTwinkleDensity, ["number"]
		command setAutoplayDuration, ["number"]
		command setCurrentPattern, ["string"]
		command setCurrentPalette, ["string"]
	}

	simulator {
	}

	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Seasonal Winter.seasonal-winter-003", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Seasonal Winter.seasonal-winter-003", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Seasonal Winter.seasonal-winter-003", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Seasonal Winter.seasonal-winter-003", backgroundColor:"#ffffff", nextState:"turningOn"
		}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}

		standardTile("auto", "auto", width: 3, height: 3, canChangeIcon: true) {
			state "off", label: 'Auto ${currentValue}', action: "autoOn",
				  icon: "st.Seasonal Winter.seasonal-winter-003", backgroundColor: "#ffffff", nextState:"turningOn"
			state "on", label: 'Auto ${currentValue}', action: "autoOff",
				  icon: "st.Seasonal Winter.seasonal-winter-003", backgroundColor: "#00a0dc", nextState:"turningOff"
			state "turningOn", label:'Turning on', icon:"st.Seasonal Winter.seasonal-winter-003", backgroundColor:"#00a0dc", nextState: "turningOff"
			state "turningOff", label:'Turning off', icon:"st.Seasonal Winter.seasonal-winter-003", backgroundColor:"#ffffff", nextState: "turningOn"                  
		}

		slider("autoplayDuration", "Autoplay duration", "setAutoplayDuration", 3, 3)

		smallSlider("speed", "Speed", "setSpeed")
		smallSlider("cooling", "Cooling", "setCooling")
		smallSlider("sparking", "Sparking", "setSparking")
		smallSlider("twinkleSpeed", "Twinkle speed", "setTwinkleSpeed")
		smallSlider("twinkleDensity", "Twinkle density", "setTwinkleDensity")

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		
		details (["switch",
				  "auto", "autoplayDuration", "speed",
				  "cooling", "sparking", "twinkleSpeed",
				  "twinkleDensity", "refresh"])
	}
}

def parse(description) {
	log(description,3)
}

def updated() {
	log("updated")
	setCurrentPattern()
	setCurrentPalette()
}

def installed() {
	log("installed")
}

def configure() {
	log("configure")
}

def setAutoplayDuration(duration){
	send("/autoplayDuration?value=$duration")
}

def setSpeed(speed){
	send("/speed?value=$speed")
}

def on() {
	send("/power?value=1")
}

def off() {
	send("/power?value=0")
}

def autoOn() {
	send("/autoplay?value=1")
}

def autoOff() {
	send("/autoplay?value=0")
}

def setLevel(level){
	def levelC = convert(level, 100, 255)
	send("/brightness?value=$levelC")
}

def setCooling(level){
	send("/cooling?value=$level")
}

def setSparking(level){
	send("/sparking?value=$level")
}

def setColor(color) {
	def colorStr = "r=$color.red&g=$color.green&b=$color.blue"
	send("/solidColor?$colorStr")
}

def setTwinkleSpeed(level){
	send("/twinkleSpeed?value=$level")
}

def setTwinkleDensity(level){
	send("/twinkleDensity?value=$level")
}

def setCurrentPattern(patternName) {
	setPattern(pattern)
}

def setCurrentPalette(patternName) {
	setPalette(palette)
}

def setPattern(patternName) {
	send("/patternName?value=$patternName")
}

def setPalette(paletteName) {
	send("/paletteName?value=$paletteName")
}

def commandsCallback(physicalgraph.device.HubResponse hubResponse){
	log("callback commandsCallback\nBody: $hubResponse.body", 3)
}

def offOn(int state){
	return [0:"off", 1:"on"][state]
}

def refreshCallback(physicalgraph.device.HubResponse hubResponse){
	try {
		def state = new groovy.json.JsonSlurper().parseText(hubResponse.body)
		log("callback refreshCallback\nBody: $hubResponse.body", 2)
		for(def member in state) {
			if (member.type != 'Section') {
				switch (member.name) {
					case 'power':
						sendEvent(name: "switch", value: offOn(member.value))
						break
					case 'autoplay':
						sendEvent(name: "auto", value: offOn(member.value))
						break
					case 'brightness':
						sendEvent(name: "level", value: convert(member.value, 255, 100))
						break
					case 'solidColor':
						sendEvent(name: "color", value: colorToHex(member.value))
						break
					case 'autoplayDuration':
						sendEvent(name: "autoplayDuration", value: member.value) 
						break
					case 'speed':
						sendEvent(name: "speed", value: member.value) 
						break
					case 'cooling':
						sendEvent(name: "cooling", value: member.value) 
						break
					case 'sparking':
						sendEvent(name: "sparking", value: member.value) 
						break
					case 'twinkleSpeed':
						sendEvent(name: "twinkleSpeed", value: member.value) 
						break
					case 'twinkleDensity':
						sendEvent(name: "twinkleDensity", value: member.value) 
						break
				}
			}
		}
	}
	catch (Exception e) {
		log("Hit Exception $e on $hubResponse", 0)
	}
}

def toInt(str) {
	str?.isInteger() ? str.toInteger() : null
}

def colorToHex(colorStr) {
	def splitted = colorStr?.split(",")
	def convertedIntValue = toInt(splitted[0]) * 256 * 256 + toInt(splitted[1]) * 256 + toInt(splitted[2])
	def result = "#" + (Integer.toHexString(convertedIntValue)).padLeft(6, '0')
	return result
}

def refresh() {
	doMethod("GET", "/all", refreshCallback)
}

def send(path) {
	log(path)
	doMethod("POST", path, commandsCallback)
	refresh()
}

def doMethod(method, path, callback) {
	def port = 80
	if (internal_port)
		port = "${internal_port}"

	try {
		path = path.replaceAll(' ', '%20')
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: "${path}",
			headers: [
				HOST: "${internal_ip}:${port}"
			],
			null,
			[callback: callback]
		)
		log(hubAction, 2) 
		sendHubCommand(hubAction)
		hubAction    
	}
	catch (Exception e) {
		log("Hit Exception $e on $hubAction", 0)
	}
}

def findConfNode(name){
	for(def member in getInitProperties()) {
		if (member.type != 'Section') {
			if (member.name == name) {
				return member
			}
		}
	}
}

def convert(value, int fromRange, int toRange){
	return Math.round(value * toRange / fromRange)
}

def log(message, level = 1) {
	if (logLevel >= level)
		log.debug(message)
}

def smallSlider(name, label, action) {
	slider(name, label, action, 1, 2)
}

def slider(name, label, action, height, width) {
	controlTile(name, name, "slider", height: height, width: width, label: label, range: "(${findConfNode(name).min}..${findConfNode(name).max})") {
		state "$name", action:"$action"
	}
}
def generateSetupSelector(name, title){
	input "$name", "enum", title: "$title", defaultValue: 0, displayDuringSetup: false, required: true, options: findConfNode("$name").options
}
