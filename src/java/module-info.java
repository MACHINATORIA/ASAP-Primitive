module asap.primitive {
	requires java.xml.bind;
	requires transitive java.desktop;

	exports asap.primitive.bits;
	exports asap.primitive.bits.bitStore;
	exports asap.primitive.bytes;
	exports asap.primitive.collection;
	exports asap.primitive.console;
	exports asap.primitive.crypto;
	exports asap.primitive.dateTime;
	exports asap.primitive.environment;
	exports asap.primitive.exception;
	exports asap.primitive.file;
	exports asap.primitive.jni;
	exports asap.primitive.lang;
	exports asap.primitive.log;
	exports asap.primitive.math;
	exports asap.primitive.pattern;
	exports asap.primitive.process;
	exports asap.primitive.string;
	exports asap.primitive.swing;
	exports asap.primitive.thread;

}