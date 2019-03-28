package liquibase.parser.mapping.core;

import liquibase.ExtensibleObject;
import liquibase.Scope;
import liquibase.exception.ParseException;
import liquibase.parser.ParsedNode;
import liquibase.parser.mapping.ParsedNodeMapping;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;

import java.lang.reflect.Type;

/**
 * Converts parsed nodes to/from non-{@link ExtensibleObject} objects by running {@link ParsedNode#getValue()} through {@link ObjectUtil#convert(Object, Class)}.
 */
public class PlainObjectParsedNodeMapping implements ParsedNodeMapping {

    @Override
    public int getPriority(ParsedNode parsedNode, Class objectType, Type containerType, String containerAttribute) {
        if (!ExtensibleObject.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public ParsedNode toParsedNode(Object objectToConvert, Class containerType, String containerAttribute, ParsedNode parentNode) throws ParseException {
        return parentNode.addChild(containerAttribute)
                .setValue(objectToConvert);
    }

    @Override
    public Object toObject(ParsedNode parsedNode, Class objectType, Class containerType, String containerAttribute) throws ParseException {
        try {
            if (parsedNode.getChildren().size() > 0) {
                throw new ParseException("Unexpected attribute(s) " + StringUtil.join(parsedNode.getChildren(), ", ", new StringUtil.StringUtilFormatter<ParsedNode>() {
                    @Override
                    public String toString(ParsedNode obj) {
                        return "'"+obj.getName()+"'";
                    }
                }) + " for " + objectType.getName(), parsedNode);
            }
            return parsedNode.getValue(null, objectType);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Error parsing '" + parsedNode.getName() + "': cannot convert value '" + parsedNode.getValue() + "' to a " + objectType.getName(), e, parsedNode);
        }
    }
}
