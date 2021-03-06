package org.erdc.cobie.shared.cobietab.transformation.cobielite;


import org.buildingsmartalliance.docs.nbims03.cobie.cobielite.IssueType;
import org.buildingsmartalliance.docs.nbims03.cobie.core.IssueCollectionType;
import org.erdc.cobie.shared.cobietab.IndexedCOBie;
import org.erdc.cobie.sheetxmldata.COBIEBaseType;

public class IssueDispatcher extends TypicalParserDispatcher<org.erdc.cobie.sheetxmldata.IssueType, IssueCollectionType, IssueType, COBIEBaseType>
{
    public IssueDispatcher(Iterable<org.erdc.cobie.sheetxmldata.IssueType> childSourceElements, COBIEBaseType targetParent,
            IndexedCOBie indexedCOBie, IssueCollectionType issues)
    {
        super(childSourceElements, targetParent, indexedCOBie);
    }

    @Override
    protected COBieTabTransformer<org.erdc.cobie.sheetxmldata.IssueType, IssueType> createNewParser(
            org.erdc.cobie.sheetxmldata.IssueType sourceElement,
            IssueType newTargetElement)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IssueType createNewTargetElement()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IssueCollectionType getTargetCollection()
    {
        // targetParent.
        // TODO Auto-generated method stub
        return null;
    }

}
