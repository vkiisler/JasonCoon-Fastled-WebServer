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
 *  Date: 2020-11-21
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
	section("Effects"){
        generateSetupSelectors()
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
		command setPattern, ["string"]
		command setPalette, ["string"]
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
	send("/brightness?value=${convert(level, 100, 255)}")
}

def setCooling(level){
	send("/cooling?value=$level")
}

def setSparking(level){
	send("/sparking?value=$level")
}

def setColor(color) {
	def rgb = huesatToRGB(color.hue as Integer, color.saturation as Integer)
    def red = rgb[0]
    def green = rgb[1]
    def blue = rgb[2]
	log(rgb)
	send("/solidColor?r=$red&g=$green&b=$blue")
    
//	send("/solidColor?r=$color.red&g=$color.green&b=$color.blue")
}

def setTwinkleSpeed(level){
	send("/twinkleSpeed?value=$level")
}

def setTwinkleDensity(level){
	send("/twinkleDensity?value=$level")
}

def setCurrentPattern() {
	setPattern(pattern.replaceAll('&', '%26'))
}

def setCurrentPalette() {
	setPalette(palette.replaceAll('&', '%26'))
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

// huesatToRGB Changed method provided by daved314
def huesatToRGB(float hue, float sat) {
	if (hue <= 100) {
		hue = hue * 3.6
    }
    sat = sat / 100
    float v = 1.0
    float c = v * sat
    float x = c * (1 - Math.abs(((hue/60)%2) - 1))
    float m = v - c
    int mod_h = (int)(hue / 60)
    int cm = Math.round((c+m) * 255)
    int xm = Math.round((x+m) * 255)
    int zm = Math.round((0+m) * 255)
    switch(mod_h) {
    	case 0: return [cm, xm, zm]
       	case 1: return [xm, cm, zm]
        case 2: return [zm, cm, xm]
        case 3: return [zm, xm, cm]
        case 4: return [xm, zm, cm]
        case 5: return [cm, zm, xm]
	}   	
}
def rgbToHSV(red, green, blue) {
	float r = red / 255f
	float g = green / 255f
	float b = blue / 255f
	float max = [r, g, b].max()
	float delta = max - [r, g, b].min()
	def hue = 13
	def saturation = 0
	if (max && delta) {
		saturation = 100 * delta / max
		if (r == max) {
			hue = ((g - b) / delta) * 100 / 6
		} else if (g == max) {
			hue = (2 + (b - r) / delta) * 100 / 6
		} else {
			hue = (4 + (r - g) / delta) * 100 / 6
		}
	}
	[hue: hue, saturation: saturation, value: max * 100]
}

def refreshCallback(physicalgraph.device.HubResponse hubResponse){
//	var String strRgb[]
	try {
		log("callback refreshCallback\nBody: $hubResponse.body", 2)
		for(def member in new groovy.json.JsonSlurper().parseText(hubResponse.body)) {
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
                        def str = member.value.toString()
                        def strRgb = member.value.split(',')
                        def hsv = rgbToHSV(strRgb[0].toInteger(), strRgb[1].toInteger(), strRgb[2].toInteger())
//						sendEvent(name: "color", value: colorToHex(member.value))
                        sendEvent(name: "saturation", value: hsv.saturation)
                        sendEvent(name: "hue", value: hsv.hue)
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
	return "#" + (Integer.toHexString(convertedIntValue)).padLeft(6, '0')
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
	}
	catch (Exception e) {
		log("Hit Exception $e on $hubAction", 0)
	}
}

def findConfNodes(nodeName, value) {
	def retArray=[]
	for(def member in getInitProperties()) {
		if (member.type != 'Section') {
			if (member[nodeName] == value) {
				retArray << member
			}
		}
	}
	return retArray
}

def findConfNode(name){
	return findConfNodes("name", name)[0]
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

def generateSetupSelector(name, title, options){
	input "$name", "enum", title: "$title", defaultValue: 0, displayDuringSetup: false, required: true, options: options
}

def generateSetupSelectors(){
	def optinNodes = findConfNodes("type", "Select")
    for (def node in optinNodes) {
		generateSetupSelector(node.name, node.label, node.options)
	}
}
