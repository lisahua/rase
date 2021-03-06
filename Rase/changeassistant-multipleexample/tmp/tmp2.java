@Override public Mapper.Builder parse(String name,Map<String,Object> node,ParserContext parserContext) throws MapperParsingException {
  TimestampFieldMapper.Builder builder=timestamp();
  parseField(builder,builder.name,node,parserContext);
  for (  Map.Entry<String,Object> entry : node.entrySet()) {
    String fieldName=Strings.toUnderscoreCase(entry.getKey());
    Object fieldNode=entry.getValue();
    if (fieldName.equals("enabled")) {
      EnabledAttributeMapper enabledState=nodeBooleanValue(fieldNode) ? EnabledAttributeMapper.ENABLED : EnabledAttributeMapper.DISABLED;
      builder.enabled(enabledState);
    }
 else     if (fieldName.equals("path")) {
      builder.path(fieldNode.toString());
    }
 else     if (fieldName.equals("format")) {
      builder.dateTimeFormatter(parseDateTimeFormatter(builder.name(),fieldNode.toString()));
    }
  }
  return builder;
}
