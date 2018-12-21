package com.wsg.protocl_7001;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Message {
	public static final byte TYPE_CONNECTION_ESTABLISH = 0x04;
	public static final byte TYPE_TERMINAL_SEND = 0x08;
	public static final byte TYPE_PLATFORM_SEND = 0x0c;

	private byte type;
	private String terminalId;
	private byte sequence;
	private byte[] data;
	private String from;
	private String to;

	private byte arithmetic;
	private byte[] key;
	private boolean encrypt;

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public byte getSequence() {
		return sequence;
	}

	public void setSequence(byte sequence) {
		this.sequence = sequence;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isType(int type) {
		return this.type == type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public byte getArithmetic() {
		return arithmetic;
	}

	public void setArithmetic(byte arithmetic) {
		this.arithmetic = arithmetic;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public byte[] getBytes() {
		byte[] fromBytes = null;
		if (from != null) {
			fromBytes = from.getBytes(Charset.forName("UTF-8"));
		}
		byte[] toBytes = null;
		if (to != null) {
			toBytes = to.getBytes(Charset.forName("UTF-8"));
		}
		byte[] terminalIdBytes = null;
		if (terminalId != null) {
			try {
				terminalIdBytes = Hex.decodeHex(terminalId.toCharArray());
			} catch (DecoderException e) {
				terminalIdBytes = null;
			}
		}
		int typeLength = 4;
		int terminalIdLength = (terminalIdBytes == null ? 0 : terminalIdBytes.length + 3);
		int sequenceLength = 4;
		int dataLength = (data == null ? 0 : data.length + 3);
		int fromLength = (fromBytes == null ? 0 : fromBytes.length + 3);
		int toLength = (toBytes == null ? 0 : toBytes.length + 3);
		int totalLength = typeLength + terminalIdLength + sequenceLength + dataLength + fromLength + toLength;

		ByteBuffer buf = ByteBuffer.allocate(totalLength);
		buf.put((byte) 'T');
		buf.putShort((short) 1);
		buf.put(type);
		if (terminalIdBytes != null) {
			buf.put((byte) 'I');
			buf.putShort((short) terminalIdBytes.length);
			buf.put(terminalIdBytes);
		}
		buf.put((byte) 'S');
		buf.putShort((short) 1);
		buf.put(sequence);
		if (data != null) {
			buf.put((byte) 'D');
			buf.putShort((short) data.length);
			buf.put(data);
		}
		if (fromBytes != null) {
			buf.put((byte) 'F');
			buf.putShort((short) fromBytes.length);
			buf.put(fromBytes);
		}
		if (toBytes != null) {
			buf.put((byte) 'O');
			buf.putShort((short) toBytes.length);
			buf.put(toBytes);
		}
		return buf.array();
	}

	public static Message valueOf(byte[] bytes) {
		if (bytes == null || bytes.length <= 3) {
			return null;
		}
		Message message = new Message();
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		while (buf.hasRemaining()) {
			char tag = (char) buf.get();
			short length = buf.getShort();
			byte[] value = new byte[length];
			buf.get(value);
			switch (tag) {
			case 'T':
				message.setType(value[0]);
				break;
			case 'I':
				message.setTerminalId(Hex.encodeHexString(value));
				break;
			case 'S':
				message.setSequence(value[0]);
				break;
			case 'D':
				message.setData(value);
				break;

			case 'F':
				message.setFrom(new String(value, Charset.forName("UTF-8")));
				break;

			case 'O':
				message.setTo(new String(value, Charset.forName("UTF-8")));
				break;
			}
		}
		return message;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
