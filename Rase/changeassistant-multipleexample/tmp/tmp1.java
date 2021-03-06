@Override public Mapper.Builder parse(String name,Map<String,Object> node,ParserContext parserContext) throws MapperParsingException {
  IndexFieldMapper.Builder builder=MapperBuilders.index();
  parseField(builder,builder.name,node,parserContext);
  for (  Map.Entry<String,Object> entry : node.entrySet()) {
    String fieldName=Strings.toUnderscoreCase(entry.getKey());
    Object fieldNode=entry.getValue();
    if (fieldName.equals("enabled")) {
      EnabledAttributeMapper mapper=nodeBooleanValue(fieldNode) ? EnabledAttributeMapper.ENABLED : EnabledAttributeMapper.DISABLED;
      builder.enabled(mapper);
    }
  }
  return builder;
}
