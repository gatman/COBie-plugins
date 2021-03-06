package org.erdc.cobie.shared.cobietab.transformation.cobielite;


import org.buildingsmartalliance.docs.nbims03.cobie.cobielite.AssetTypeInfoType;
import org.buildingsmartalliance.docs.nbims03.cobie.cobielite.SpareDocument;
import org.buildingsmartalliance.docs.nbims03.cobie.cobielite.SpareType;
import org.buildingsmartalliance.docs.nbims03.cobie.core.SpareCollectionType;
import org.erdc.cobie.shared.cobietab.IndexedCOBie;

public class AssetTypeSpareDispatcher extends
        TypicalParserDispatcher<org.erdc.cobie.sheetxmldata.SpareType, SpareCollectionType, SpareType, AssetTypeInfoType>
{

    public AssetTypeSpareDispatcher(Iterable<org.erdc.cobie.sheetxmldata.SpareType> childSourceElements, AssetTypeInfoType targetParent,
            IndexedCOBie indexedCOBie)
    {
        super(childSourceElements, targetParent, indexedCOBie);
    }

    @Override
    protected COBieTabTransformer<org.erdc.cobie.sheetxmldata.SpareType, SpareType> createNewParser(
            org.erdc.cobie.sheetxmldata.SpareType sourceElement,
            SpareType newTargetElement)
    {
        return new SpareTransformer(sourceElement, newTargetElement, indexedCOBie);
    }

    @Override
    protected SpareType createNewTargetElement()
    {
        return (SpareType)targetCollection.addNewSpare().substitute(SpareDocument.type.getDocumentElementName(), SpareType.type);
    }

    @Override
    protected SpareCollectionType getTargetCollection()
    {
        SpareCollectionType spares = targetParent.getSpares();
        if (spares == null)
        {
            spares = targetParent.addNewSpares();
        }
        return spares;
    }

}
