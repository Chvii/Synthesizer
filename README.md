# **What is this???**

A very simple polyphonic synthesizer made in Java in a single afternoon.
Wanted to learn how threads and audio processing (could) be done in Java, since most VST's and synthesizer software is written in C++.

This is by no means pretty or well-organised code (and I am sure my professors wouldn't be happy to see how I mistreat their strategy patterns for the sake of getting it to work as
fast as possible), but it does what a very (VERY) basic synthesizer should do. Has the 4 standard wavetable forms (sine, triangle, saw, square). And allows for easy integration of new wavetables. (I tried getting this to work with a GUI button for switching wavetables - but for some reason the JFrame button broke the sound engine! (It took a long time to figure out that the button was the culprit behind the sound suddenly not working, I guess this is why we unit test)).

* The waveform can (in this iteration) be switched with the '$' key
* Switch octave with '+' (Octave up) and '-' (Octave down).

* Notes are linked to the keyboard, FL Studio style:
q: C
2: C#
w: D
3: D#
e: E
r: F
5: F#
t: G
6: G#
y: A
7: A#
u: B
i: C
9: C#
o: D
0: D#
p: E

## ROADMAP
* Use float instead of byte for cleaner audio.
* Add ADSR functionality.
* Add different kinds of waveform mutators (FM, RM, the whole lot).
* Add support for MIDI (NO idea how to do that -- yet).
* Make a GUI for interacting with the synthesizer.
* Implement different effects (reverb, delay, saturation, chorus).
* Add a sequencer with an intuitive GUI.
* Figure out how to make a filter.
* Re-assignable LFO.
* Add multiple oscillators.
* Add granular synthesis functionality (because granular synths are cool and not enough people know about them).
* Unison & Detune functionality.
* Fun home-baked wavetables.
* Monophonic mode with glide.
* Reassignable mod-wheel

## KNOWN ISSUES
* **Playing two or more notes at the same time produces significantly less volume than playing a single note**
_I have been ripping my hair out trying to figure out why this happens. I guess it's probably due to me using multiple things in a way they aren't supposed to be used_
* **Waveform display looks like crud**
_Again, been trying a few different things to make the waveform display smoother, but I am yet to find a good solution. I'm guessing that it comes down to some way of taking a few samples and then showing the average waveform produced by those samples, and then take another batch of samples, and make a gliding transition towards displaying a waveform for those samples(?), but I will probably have to just deep-dive into someone else's project where they've made a good waveform oscilloscope._
* **No ADSR or way to control/change the envelope**
_This is the next feature I am going to focus on implementing. But I am pretty sure this exact feature is going to bite me, and force a refactoring of the whole codebase. So that will be fun! :)_
* **What is the point of this?**
_Not sure. Maybe if I end up making a cool synthesizer that does something neat, I can turn it into a vst plugin. (Whether that means that I have to learn C++ and then rewrite the whole thing in C++; I am not sure... But I hope not)._


