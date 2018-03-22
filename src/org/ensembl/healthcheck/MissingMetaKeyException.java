package org.ensembl.healthcheck;

public class MissingMetaKeyException extends Exception {
	public MissingMetaKeyException(String msg) {
		super(msg);
	}
}
