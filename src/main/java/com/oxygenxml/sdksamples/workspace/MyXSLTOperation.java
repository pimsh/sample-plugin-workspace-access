package com.oxygenxml.sdksamples.workspace;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.commons.operations.TransformOperation;
import ro.sync.ecss.extensions.commons.operations.XSLTOperation;

public class MyXSLTOperation extends XSLTOperation{

	@Override
	protected Transformer createTransformer(AuthorAccess authorAccess, Source scriptSrc)
			throws TransformerConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

}
