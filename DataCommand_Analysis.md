# DataCommand 詳細代碼解析

## 1. 類別聲明和常量定義

```java
public class DataCommand {
   // 合併操作失敗時的異常
   private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.merge.failed"));
   
   // 獲取無效路徑時的異常 (動態訊息，包含路徑參數)
   private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType((path) -> {
      return Text.stringifiedTranslatable("commands.data.get.invalid", new Object[]{path});
   });
   
   // 獲取未知路徑時的異常
   private static final DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION = new DynamicCommandExceptionType((path) -> {
      return Text.stringifiedTranslatable("commands.data.get.unknown", new Object[]{path});
   });
   
   // 獲取多個結果時的異常（應該只返回一個）
   private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.get.multiple"));
   
   // 修改操作期望對象類型時的異常
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_OBJECT_EXCEPTION = new DynamicCommandExceptionType((nbt) -> {
      return Text.stringifiedTranslatable("commands.data.modify.expected_object", new Object[]{nbt});
   });
   
   // 修改操作期望值類型時的異常
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_VALUE_EXCEPTION = new DynamicCommandExceptionType((nbt) -> {
      return Text.stringifiedTranslatable("commands.data.modify.expected_value", new Object[]{nbt});
   });
   
   // 字串截取無效索引時的異常
   private static final Dynamic2CommandExceptionType MODIFY_INVALID_SUBSTRING_EXCEPTION = new Dynamic2CommandExceptionType((startIndex, endIndex) -> {
      return Text.stringifiedTranslatable("commands.data.modify.invalid_substring", new Object[]{startIndex, endIndex});
   });
```

## 2. 靜態變量和初始化

```java
   // 對象類型工廠列表：實體、方塊、儲存
   public static final List<Function<String, ObjectType>> OBJECT_TYPE_FACTORIES;
   
   // 目標對象類型列表（用於操作的目標）
   public static final List<ObjectType> TARGET_OBJECT_TYPES;
   
   // 源對象類型列表（用於獲取數據的來源）
   public static final List<ObjectType> SOURCE_OBJECT_TYPES;

   static {
      // 初始化三種數據對象工廠：實體、方塊、儲存
      OBJECT_TYPE_FACTORIES = ImmutableList.of(
         EntityDataObject.TYPE_FACTORY,    // 實體數據
         BlockDataObject.TYPE_FACTORY,     // 方塊數據  
         StorageDataObject.TYPE_FACTORY    // 儲存數據
      );
      
      // 將工廠轉換為目標對象類型
      TARGET_OBJECT_TYPES = OBJECT_TYPE_FACTORIES.stream()
         .map(factory -> factory.apply("target"))
         .collect(ImmutableList.toImmutableList());
         
      // 將工廠轉換為源對象類型
      SOURCE_OBJECT_TYPES = OBJECT_TYPE_FACTORIES.stream()
         .map(factory -> factory.apply("source"))
         .collect(ImmutableList.toImmutableList());
   }
```

## 3. 主要方法解析

### register() 方法
- **作用**: 註冊 `/data` 指令到指令分發器
- **權限**: 需要等級2的權限（OP權限）
- **子指令**: merge, get, remove, modify

### executeGet() 方法群
```java
// 獲取完整NBT數據
private static int executeGet(ServerCommandSource source, DataCommandObject object)

// 獲取指定路徑的NBT數據
private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPath path)

// 獲取指定路徑的NBT數據並按比例縮放
private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPath path, double scale)
```

### executeMerge() 方法
- **作用**: 將NBT數據合併到目標對象
- **檢查**: 防止數據嵌套過深
- **回饋**: 發送操作成功訊息

### executeRemove() 方法
- **作用**: 從目標對象移除指定路徑的數據
- **返回**: 被移除的元素數量

### executeModify() 方法
- **作用**: 修改目標對象的NBT數據
- **操作類型**: insert, prepend, append, set, merge
- **路徑操作**: 支援複雜的NBT路徑操作

## 4. 輔助方法

### asString() 方法
- **作用**: 將NBT元素轉換為字串
- **支援類型**: NbtString, NbtPrimitive
- **異常**: 不支援的類型會拋出異常

### substring() 方法群
- **作用**: 字串截取操作
- **支援**: 正負索引（負數從末尾計算）
- **驗證**: 索引範圍有效性檢查

### getValues() 和 getValuesByPath()
- **作用**: 從數據對象獲取NBT值
- **差異**: 一個獲取完整數據，一個按路徑獲取
