
XleTView is distributed under the The GNU General Public License (GPL) wich can be found in a separate document.

This is a software for viewing MHP Xlets on a PC. It is NOT a complete MHP implementation and shall not be treated as such.


XleTView uses the following software:
NanoXML parser (Marc De Scheemaecker, http://nanoxml.n3.net)
Javassist (Shigeru Chiba, http://www.csg.is.titech.ac.jp/~chiba/javassist/)
Metouia look & feel(Taoufik Romdhane, (http://mlf.sourceforge.net/)


DVB and MHP are registered trademarks of the DVB Project.
http://www.mhp.org
http://www.dvb.org

JavaTV is a Sun Microsystems, Inc. trademark.


When XleTView loads your Xlet it modifies the Xlet's bytecode. 
These modifications happens only in the memory of the Virtual Machine, but you should be aware of it.


some of the changes in 0.3.6

- implementations of HText, HIcon, HAnimation, HStaticAnimation, HAnimateLook, HGraphicButton, HTextButton
  HSinglelineEntryLook, HSinglelineEntry + more...
- New customizable remote control
- fixed bugs in org.dvb.net.rc.ConnectionRCInterface, HStaticText, HStaticIcon + more



Requirements:
- Sun's sdk 1.4

Install:

- Put the files where you want them.
- Double click on xletview.jar (Windows) or run from console
  If you do the double clicking you will not get debug output. So if you want to see
  your "sysouts" you have to run from console
(If you have Sun's sdk 1.4 but not as the default runtime you need to start from console)
- Some settings can be changed in config/settings.txt


Thanks to Cristian Suazo, Christian Köberl and Bengt Skogvall 
for code contributions and Oskar Goransson for graphics. 

Others I'd like to thank are Per Bjork, Fred Grott, Kirk Bateman, Mikael Ostberg, 
Davy De Schrijver, Mattias Bergstrom, Emmanuele Brunelli, Pehr Assarsson, Emanuele Casiraghi, 
Moreno Marasco, Henrik Grehn, Igor Calic, Andrea Passerini and Giorgio Giunta.


Martin Sveden
beiker@users.sourceforge.net

Read more on the website:
http://xletview.sourceforge.net



	


