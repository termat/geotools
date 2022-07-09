package net.termat.geo.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class Image {
	@DatabaseField(generatedId=true)
	public long id;

	@DatabaseField
	public long key;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    public byte[] imageBytes;
}
