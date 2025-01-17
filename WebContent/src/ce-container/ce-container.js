/**
 * @license Copyright 2018,Instituto Federal do Rio Grande do Sul (IFRS)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
class CooperativeEditorContainer extends CooperativeEditorContainerLocalization {

	static get is() {
		return 'ce-container';
	}

	static get properties() {

		return {
			element: {
				type: String,
				notify: true,
				observer: '_elementChanged',
			},
			/**
			 * The URL of the websocket
			 */
			wsUrl: {
				type: String,
				value: 'ws:'
			},
			/**
			 * Where messages from the components arrive to be sent by the websocket
			 */
			sendMessage: {
				type: Object,
				observer(json) { this.$.ws.send(json) },
				notify: true
			}
		};
	}

	constructor() {
		super();
		this.addEventListener("openDialog", (e) => { this._openDialog(e.detail) });
		this._initSound();
		document.addEventListener("mouseout", (e) => {
			var from = e.relatedTarget;
			if (!from || from.nodeName === "HTML") {
				//this.playTTS(this.localize('helpMouseOut'));
			}
		}, false);
	}

	connectedCallback() {
		super.connectedCallback();
		this._initShortcut();
		this._initUrlWs();
	}

	/**
	 * Shortcuts made available
	 */
	_helpOpen() {
		var content =
			this.localize('shortcut6') + "<br/>" +
			this.localize('shortcut3') + "<br/>" +
			this.localize('shortcut5') + "<br/>" +
			this.localize('shortcut') + "<br/>" +
			this.localize('shortcut2') + "<br/>" +
			this.localize('shortcut4');
		this._openDialog(content);
	}

	/**
	 * Private method that prepares and opens the dialog
	 * 
	 * @param content String the content of the dialogue
	 */
	_openDialog(content) {
		this.$.dialog.lastElementChild.innerHTML = content;
		this.$.dialog.open();
	}

	/**
	 * To return to the form page, when the button is pressed
	 */
	_arrowBack() {
		window.location.href = this._resumeUrlBase();
	}

	/**
	 * Returns to base url
	 */
	_resumeUrlBase() {
		return this.rootPath;
	}

	/**
	 * When an element is selected in the context menu
	 * 
	 * @param elementName String element name
	 */
	_elementChanged(elementName) {
		// Load element import on demand. Show 404 page if fails
		var resolvedPageUrl = this.resolveUrl('../ce-' + elementName + '/ce-' + elementName + '.html');
		Polymer.importHref(
			resolvedPageUrl,
			null,
			this._showPage404.bind(this),
			true);

		// Close a non-persistent drawer when the element are changed.
		if (!this.$.drawer.persistent) {
			this.$.drawer.close();
		}
	}

	/**
	 * Element page error
	 */
	_showPage404() {
		this.element = 'error';
	}

	_initUrlWs() {
		var pathname = window.location.href;
		var hash = pathname.substr(pathname.lastIndexOf("/"));
		var base = pathname.substr(pathname.indexOf("//"), pathname.lastIndexOf("editor") - pathname.indexOf("//"));
		this.wsUrl += base + "editorws" + hash;
		setTimeout(() => this.$.ws.open(), 500);
	}

	/**
	 * Prepares as shortcut keys
	 */
	_initShortcut() {
		var ceParticipants = this.$.ceParticipants;
		var soundChat = this.$.soundChat;
		var ceEditor = this.$.ceEditor;
		var dialog = this.$.dialog;

		document.onkeyup = function (e) {
			var key = e.which || e.keyCode;
			if (key === 27) {
				dialog.close();
			} else
				if (e.shiftKey && e.altKey) {
					switch (key) {
						case 49:
							ceParticipants.readComponentStatus();
							break;
						case 50:
							ceEditor.$.ceRubric.readComponentStatus();
							break;
					}
				} else
					if (e.ctrlKey) {
						switch (key) {
							case 49:
								ceParticipants.setFocus();
								break;
							case 50:
								soundChat.setFocus();
								break;
							case 51:
								ceEditor.setFocus();
								break;
						}
					} else
						if (e.altKey && key >= 49 && key <= 57) {
							key = key - 48;
							soundChat.readLatestMessages(key);
						}
		}
	}

	_initSound() {
		this._initSoundTypes();
		this._initWebAudio();
		this._loadEffects();
		this._initSoundDefaultValues();
	}

	_initSoundDefaultValues() {
		// The sound on by default
		this.soundOn = true;
		// Auditory Icons and Earcons
		this.auditoryOn = true;
		// Auditory Icons and Earcons effects
		this.auditoryEffectOn = false;
		// Spatial sound configuration
		this.auditorySpatialOn = false;

		// TTS configurations
		this.ttsOn = true;
		this.ttsSpeed = 1.6;
		this.ttsVolume = 1;
	}

	_initSoundTypes() {
		// Sounds Types
		this.soundTypes = new Array();
	}

	_initWebAudio() {

		// Web Audio API
		this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();

		// Sound Chat sounds
		var soundChatSoundsURL = this._resumeUrlBase() + "src/sound-chat/sounds/";
		this._loadAudioBuffer("connect", this.audioCtx, soundChatSoundsURL + "CNX01_1.ogg");
		this._loadAudioBuffer("connect", this.audioCtx, soundChatSoundsURL + "CNX01_2.ogg");
		this._loadAudioBuffer("connect", this.audioCtx, soundChatSoundsURL + "CNX01_3.ogg");
		this._loadAudioBuffer("connect", this.audioCtx, soundChatSoundsURL + "CNX01_4.ogg");
		this._loadAudioBuffer("sendMessage", this.audioCtx, soundChatSoundsURL + "MSG_001.wav");
		this._loadAudioBuffer("typing", this.audioCtx, soundChatSoundsURL + "ECT_001.mp3");

		// Editor sounds
		var editorSoundsURL = this._resumeUrlBase() + "src/ce-editor/sounds/";
		this._loadAudioBuffer("startParticipation", this.audioCtx, editorSoundsURL + "quite-impressed.mp3");
		this._loadAudioBuffer("endParticipation", this.audioCtx, editorSoundsURL + "unconvinced.mp3");
		this._loadAudioBuffer("nextContribution", this.audioCtx, editorSoundsURL + "knuckle.mp3");
		this._loadAudioBuffer("acceptedRubric", this.audioCtx, editorSoundsURL + "appointed.mp3");
		this._loadAudioBuffer("moveCursor", this.audioCtx, editorSoundsURL + "scratch.mp3");
	}



	/**
   * Method used load all effects
   */
	_loadEffects() {

		// Sound Effects
		var tuna = new Tuna(this.audioCtx);
		this.delay = new tuna.Delay({
			feedback: 0.6,    // 0 to 1+
			delayTime: 100,   // 1 to 10000 milliseconds
			wetLevel: 0.8,    // 0 to 1+
			dryLevel: 1,      // 0 to 1+
			cutoff: 2000,     // cutoff frequency of the built in lowpass-filter.
			// 20 to 22050
			bypass: 0
		});

		this.wahwah = new tuna.WahWah({
			automode: true,                // true/false
			baseFrequency: 0.5,            // 0 to 1
			excursionOctaves: 2,           // 1 to 6
			sweep: 0.2,                    // 0 to 1
			resonance: 10,                 // 1 to 100
			sensitivity: 0.8,              // -1 to 1
			bypass: 0
		});

		this.moog = new tuna.MoogFilter({
			cutoff: 0.4,    // 0 to 1
			resonance: 3,   // 0 to 4
			bufferSize: 4096  // 256 to 16384
		});
	}

	/**
* Creates the source buffer
*/
	_createSourceBuffer(soundType) {
		// Selects the right buffer
		var bufferSource = this.audioCtx.createBufferSource();
		if (this.soundTypes[soundType] !== undefined) {
			bufferSource.buffer = this.soundTypes[soundType];
		} else {
			console.error("soundType not find " + soundType + ", sound available " + Object.keys(this.soundTypes));
		}

		return bufferSource;
	}


	_loadAudioBuffer(bufferType, audioCtx, url) {

		var request = new XMLHttpRequest();
		request.open("GET", url, true);
		request.responseType = 'arraybuffer';
		request.onload = () => {
			audioCtx.decodeAudioData(request.response).then((decodedData) => {
				this.soundTypes[bufferType] = decodedData;
			},
				function (e) {
					console.error("Decode audio data error:" + e.err);
				});
		}
		request.send();
	}

	/**
* Method used to play a sound depending on the sound control options set by
* users.
* 
* @param String
*          audio : The audio that the system wants to play. It's related
*          with the audios loaded on the system
* @param String
*          intention : Verify the action (intention) the system wants to
*          play
*/
	playSound(intention, position, timbre) {
		if (this.auditoryOn) {
			if (timbre != null) {
				this.playSoundWithTimbre(intention, position, timbre);
			}
		}

		playSoundWithTimbre(soundType, position, timbre); {
			this.audioCtx.resume();

			var bufferSource = this._createSourceBuffer(soundType);
			var stereoPanner = this._createStereoPanner(position);

			if (this.soundOn) {
				if (timbre === "TIMBRE_1") {
					bufferSource.connect(stereoPanner);
					stereoPanner.connect(this.audioCtx.destination);
				}
				else if (timbre === "TIMBRE_2") {
					bufferSource.connect(stereoPanner);
					stereoPanner.connect(this.audioCtx.destination);
				}
				else if (timbre === "TIMBRE_3") {
					bufferSource.connect(stereoPanner);
					stereoPanner.connect(this.audioCtx.destination);
				}
				else if (timbre === "TIMBRE_4") {
					bufferSource.connect(stereoPanner);
					stereoPanner.connect(this.audioCtx.destination);
				}
			}
			else {
				bufferSource.connect(stereoPanner);
				stereoPanner.connect(this.audioCtx.destination);
			}

			// Plays the sound
			bufferSource.start();
		}

		/**
		 * Creates the spatial schema of the sound
		 */
		_createStereoPanner(position); {
			var stereoPanner = this.audioCtx.createStereoPanner();
			if (this.auditorySpatialOn) {
				var positionValue = (position === "LEFT") ? 1 : -1;
				stereoPanner.pan.setTargetAtTime(positionValue, this.audioCtx.currentTime, 0);
			}
			else {
				stereoPanner.pan.setTargetAtTime(0, this.audioCtx.currentTime, 0);
			}
			return stereoPanner;
		}
	}
	/**
* Method used to execute text-to-speech
*/
	playTTS(msg) {
		if (this.ttsOn) {
			var ttsObject = new SpeechSynthesisUtterance(msg);
			ttsObject.lang = this.getAccent(this.language);
			// Adjust speed and volume
			ttsObject.rate = this.ttsSpeed;
			ttsObject.volume = this.ttsVolume;
			speechSynthesis.speak(ttsObject);
		}
		return this.ttsOn;
	}

	/**
   * Get the default accent to TTS
   */
	getAccent(language) {
		var accent;
		switch (language) {
			case 'pt':
				accent = 'pt-BR';
				break;
			case 'en':
				accent = 'en-US';
				break;
			default:
				accent = language;
		}
		return accent;
	}

	_browse() {
		this.$.ws.send({ type: 'BROWSE' });
	}

}

window.customElements.define(CooperativeEditorContainer.is, CooperativeEditorContainer);