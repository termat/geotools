package net.termat.geo.citygml;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.factory.DimensionMismatchException;
import org.citygml4j.factory.GMLGeometryFactory;
import org.citygml4j.geometry.BoundingBox;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.citygml.generics.DoubleAttribute;
import org.citygml4j.model.citygml.generics.IntAttribute;
import org.citygml4j.model.citygml.generics.MeasureAttribute;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.Measure;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.util.bbox.BoundingBoxOptions;
import org.citygml4j.xml.io.CityGMLOutputFactory;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityGMLWriter;
import org.locationtech.jts.io.ParseException;

import com.google.gson.JsonObject;

import net.termat.geo.geojson.GeojsonProcesser;
import net.termat.geo.util.LonLatXY;

public class BldgCreator {
	private GeojsonProcesser geo;
	private int coordSys;
	private Map<String,String> spec;
	
	public BldgCreator(File f,int cs) throws IOException, ParseException, CityGMLBuilderException {
		geo=new GeojsonProcesser(f);
		this.coordSys=cs;
		spec=getCodeListSpecMap();
	}
	
	private Map<String,String> getCodeListSpecMap(){
		Map<String,String> ret=new HashMap<>();
		ret.put("class", "../../codelists/Building_class.xml");
		ret.put("usages", "../../codelists/Building_usage.xml");
		ret.put("roofType", "../../codelists/Building_roofType.xml");
		return ret;
	}
	
	public void create(File outfile,String[] prop) throws DimensionMismatchException, CityGMLWriteException, CityGMLBuilderException {
		CityGMLContext ctx = CityGMLContext.getInstance();
		CityGMLBuilder builder = ctx.createCityGMLBuilder();		
		CityModel cityModel = new CityModel();
		Point low=null,up=null;
		int n=geo.getShapes().size();
		for(int i=0;i<n;i++) {
			Shape s=geo.getShape(i);
			JsonObject p=geo.getProperty(i);
			Building bldg=createBuilding(s,p,prop);
			BoundingShape bs=bldg.calcBoundedBy(BoundingBoxOptions.defaults());
			BoundingBox bb=bs.getEnvelope().toBoundingBox();
			if(low==null) {
				low=bb.getLowerCorner();
			}else {
				Point ll=bb.getLowerCorner();
				low.setX(Math.min(low.getX(), ll.getX()));
				low.setY(Math.min(low.getY(), ll.getY()));
				low.setZ(Math.min(low.getZ(), ll.getZ()));
			}
			if(up==null) {
				up=bb.getUpperCorner();
			}else {
				Point ll=bb.getLowerCorner();
				up.setX(Math.max(up.getX(), ll.getX()));
				up.setY(Math.max(up.getY(), ll.getY()));
				up.setZ(Math.max(up.getZ(), ll.getZ()));
			}
//			cityModel.setBoundedBy(bldg.calcBoundedBy(BoundingBoxOptions.defaults()));
			cityModel.addCityObjectMember(new CityObjectMember(bldg));
		}
		BoundingBox bb=new BoundingBox();
		bb.setLowerCorner(low);
		bb.setUpperCorner(up);
		Envelope env=new BoundingShape(bb).getEnvelope();
		env.setSrsName("http://www.opengis.net/def/crs/EPSG/0/6697");
		cityModel.setBoundedBy(new BoundingShape(env));
		CityGMLOutputFactory out = builder.createCityGMLOutputFactory(CityGMLVersion.DEFAULT);
		CityGMLWriter writer = out.createCityGMLWriter(outfile, "UTF-8");
		writer.setPrefixes(CityGMLVersion.DEFAULT);
		writer.setSchemaLocations(CityGMLVersion.DEFAULT);
		writer.setIndentString("  ");
		writer.write(cityModel);
		writer.close();	
	}
	
	
	private Building createBuilding(Shape geo,JsonObject prop,String[] col) throws DimensionMismatchException {
		GMLGeometryFactory geom = new GMLGeometryFactory();
		Building building = new Building();
		List<Point2D> pt=getPoints(geo);
		if(pt.size()<3)return null;
		double dchm=prop.get("measuredHeight").getAsDouble();
		double dem=prop.get("dem").getAsDouble();
		Polygon ground = createGroundRoof(geom,pt,dem);
		List<Polygon> wall=createWall(geom,pt,dem,dem+dchm);
		Polygon roof = createGroundRoof(geom,pt,dchm+dem);
		for(String str : col) {
			if(str.equals("name")) {
				setStringAttribute(building,str,prop.get(str).getAsString());
			}else if(str.equals("class")) {
				setClassAttr(building,spec.get(str),prop.get(str).getAsString());
			}else if(str.equals("usage")) {
				setUsageAttr(building,spec.get(str),prop.get(str).getAsString());
			}else if(str.equals("yearOfConstruction")) {
				building.setYearOfConstruction(prop.get(str).getAsInt());
			}else if(str.equals("yearOfDemolition")) {
				building.setYearOfDemolition(prop.get(str).getAsInt());
			}else if(str.equals("roofType")) {
				setRoofAttr(building,spec.get(str),prop.get(str).getAsString());
			}else if(str.equals("address")) {
				AddressDetails addressDetails=new AddressDetails();
				Country ct=new Country();
				CountryName cn=new CountryName();
				cn.setContent("日本");
				ct.addCountryName(cn);
				addressDetails.setCountry(ct);
				Locality lo=new Locality();
				LocalityName ln=new LocalityName();
				ln.setContent(prop.get(str).getAsString());
				ln.setType("Town");
				lo.addLocalityName(ln);
				addressDetails.setLocality(lo);
				Address address = new Address();
				XalAddressProperty xalAddressProperty = new XalAddressProperty();
				xalAddressProperty.setAddressDetails(addressDetails);
				address.setXalAddress(xalAddressProperty);
				AddressProperty addressProperty = new AddressProperty();
				addressProperty.setObject(address);
				List<AddressProperty> li=new ArrayList<>();
				li.add(addressProperty);
				building.setAddress(li);
			}else if(str.equals("measuredHeight")) {
		        Length ll=new Length();
		        ll.setUom("m");
		        ll.setValue(dchm);
		        building.setMeasuredHeight(ll);
			}else if(str.equals("dem")) {

				
				
			}else if(str.equals("storeysAboveGround")) {
				MeasureOrNullList ma=new MeasureOrNullList();
				DoubleOrNull mn=new DoubleOrNull();
				mn.setDouble(prop.get(str).getAsDouble());
				ma.addDoubleOrNull(mn);
		        building.setStoreyHeightsAboveGround(ma);
			}else if(str.equals("storeysBelowGround")) {
				MeasureOrNullList ma=new MeasureOrNullList();
				DoubleOrNull mn=new DoubleOrNull();
				mn.setDouble(prop.get(str).getAsDouble());
				ma.addDoubleOrNull(mn);
		        building.setStoreyHeightsBelowGround(ma);
			}else if(str.equals("建物ID")) {
				setStringAttribute(building,str,prop.get(str).getAsString());
			}else if(str.equals("枝番")) {
				setIntAttribute(building,str,prop.get(str).getAsInt());
			}else if(str.equals("建物用途コード番号")) {
				setStringAttribute(building,str,prop.get(str).getAsString());
			}else if(str.equals("建蔽率")||str.equals("容積率")||str.equals("指定建蔽率")||str.equals("指定容積率")||str.equals("基準容積率")) {
				setDoubleAttribute(building,str,prop.get(str).getAsDouble());
			}else {
				String[] val=str.split(",");
				int code=Integer.parseInt(val[1]);
				switch(code) {
					case 0:
						continue;
					case 1:
						setIntAttribute(building,val[0],prop.get(val[0]).getAsInt());
						break;
					case 2:
						setDoubleAttribute(building,val[0],prop.get(val[0]).getAsDouble());
						break;
					case 3:
						setStringAttribute(building,val[0],prop.get(val[0]).getAsString());
						break;
					default:
						setMesuramentAttribute(building,val[0],prop.get(val[0]).getAsDouble(),val[2]);
						break;
				}
			}
		}
        building.setLod0FootPrint(new MultiSurfaceProperty(new MultiSurface(ground)));
        building.setLod0RoofEdge(new MultiSurfaceProperty(new MultiSurface(roof)));
		List<SurfaceProperty> surfaceMember = new ArrayList<>();
		surfaceMember.add(new SurfaceProperty(ground));
		for(Polygon pl : wall) {
			surfaceMember.add(new SurfaceProperty(pl));
		}
		surfaceMember.add(new SurfaceProperty(roof));
				CompositeSurface compositeSurface = new CompositeSurface();
		compositeSurface.setSurfaceMember(surfaceMember);
		Solid solid = new Solid();
		solid.setSrsDimension(3);
		solid.setSrsName("http://www.opengis.net/def/crs/EPSG/0/6697");
		solid.setExterior(new SurfaceProperty(compositeSurface));
		building.setLod1Solid(new SolidProperty(solid));
		return building;
	}
	
	public BoundarySurfaceProperty createBoundarySurface(CityGMLClass type, Polygon geometry) {
		AbstractBoundarySurface boundarySurface = null;
		switch (type) {
		case BUILDING_WALL_SURFACE:
			boundarySurface = new WallSurface();
			break;
		case BUILDING_ROOF_SURFACE:
			boundarySurface = new RoofSurface();
			break;
		case BUILDING_GROUND_SURFACE:
			boundarySurface = new GroundSurface();
			break;
		default:
			break;
		}
		if (boundarySurface != null) {
			boundarySurface.setLod2MultiSurface(new MultiSurfaceProperty(new MultiSurface(geometry)));
			return new BoundarySurfaceProperty(boundarySurface);
		}
		return null;
	}
	
	private List<Polygon> createWall(GMLGeometryFactory geom,List<Point2D> l,double z1,double z2) throws DimensionMismatchException {
		List<Polygon> ret=new ArrayList<>();
		int n=l.size();
		for(int i=1;i<n;i++) {
			Point2D p1=l.get(i-1);
			Point2D p2=l.get(i);
			double[] p=new double[3*4];
			p[0]=p1.getX();
			p[1]=p1.getY();
			p[2]=z1;
			p[3]=p2.getX();
			p[4]=p2.getY();
			p[5]=z1;
			p[6]=p2.getX();
			p[7]=p2.getY();
			p[8]=z2;
			p[9]=p1.getX();
			p[10]=p1.getY();
			p[11]=z2;
			ret.add(geom.createLinearPolygon(p,3));
		}
		return ret;
	}
	
	private Polygon createGroundRoof(GMLGeometryFactory geom,List<Point2D> l,double z) throws DimensionMismatchException {
		int n=l.size();
		double[] p=new double[(n-1)*3];
		int it=0;
		for(int i=0;i<n-1;i++) {
			Point2D pt=l.get(i);
			p[it++]=pt.getX();
			p[it++]=pt.getY();
			p[it++]=z;
		}
		return geom.createLinearPolygon(p,3);
	}
	
	private List<Point2D> counterList(List<Point2D> l){
		List<Point2D> ret=new ArrayList<>();
		while(l.size()>0) {
			ret.add(l.remove(l.size()-1));
		}
		return ret;
	}
	
	private boolean isAntieClockWise(List<Point2D> p) {
		Point2D p1=p.get(0);
		Point2D p2=p.get(1);
		Point2D p3=p.get(2);
		double[] v1=new double[] {p2.getX()-p1.getX(),p2.getY()-p1.getY(),0};
		double[] v2=new double[] {p3.getX()-p1.getX(),p3.getY()-p1.getY(),0};
		double[] pd=crossProduct(v1,v2);
		if(pd[2]<0) {
			return false;
		}else {
			return true;
		}
	}
	
	private static double[] crossProduct(double[] a, double[] b) {  
	    double[] entries = new double[] {
	          a[1] * b[2] - a[2] * b[1],
	          a[2] * b[0] - a[0] * b[2],
	          a[0] * b[1] - a[1] * b[0]};
	    return entries;
	}
	
	private List<Point2D> getPoints(Shape sp){
		List<Point2D> ret=new ArrayList<>();
		PathIterator pi=sp.getPathIterator(AffineTransform.getScaleInstance(1.0, 1.0));
		double[] p=new double[6];
		while(!pi.isDone()){
			switch(pi.currentSegment(p)){
				case PathIterator.SEG_MOVETO:
					ret.add(new Point2D.Double(p[0],p[1]));
					break;
				case PathIterator.SEG_LINETO:
					ret.add(new Point2D.Double(p[0],p[1]));
					break;
//				case PathIterator.SEG_QUADTO:
//					ret.add(new Point2D.Double(p[2],p[3]));
//					break;
//				case PathIterator.SEG_CUBICTO:
//					ret.add(new Point2D.Double(p[4],p[5]));
//					break;
				case PathIterator.SEG_CLOSE:
					Point2D c=ret.get(0);
					ret.add(new Point2D.Double(c.getX(),c.getY()));
					break;
			}
			pi.next();
		}
		if(isAntieClockWise(ret)) {
			return transLonlat(ret);
		}else {
			return transLonlat(counterList(ret));
		}
	}
	
	private List<Point2D> transLonlat(List<Point2D> l){
		List<Point2D> ret=new ArrayList<>();
		for(Point2D p : l) {
			Point2D pt=LonLatXY.xyToLonlat(coordSys, p.getX(), p.getY());
			ret.add(new Point2D.Double(pt.getY(),pt.getX()));
		}
		return ret;
	}
	

	private void setIntAttribute(Building building,String name,int val) {
		IntAttribute sa=new IntAttribute();
        sa.setName(name);
        sa.setValue(val);        
        building.addGenericAttribute(sa);
	}
	
	private void setDoubleAttribute(Building building,String name,double val) {
		DoubleAttribute sa=new DoubleAttribute();
        sa.setName(name);
        sa.setValue(val);        
        building.addGenericAttribute(sa);
	}
	
	private void setStringAttribute(Building building,String name,String val) {
		StringAttribute sa=new StringAttribute();
        sa.setName(name);
        sa.setValue(val);        
        building.addGenericAttribute(sa);
	}
	
	private void setMesuramentAttribute(Building building,String name,double val,String uom) {
		MeasureAttribute ma=new MeasureAttribute();
		Measure mo=new Measure(val);
		mo.setUom(uom);
		ma.setName(name);
        ma.setValue(mo);
        building.addGenericAttribute(ma);
	}
	
	private void setClassAttr(Building building,String name,String val) {
		val=val.replaceAll("\"", "");
		val=BldgConstants.getClassVal(val);
		Code code=new Code();
		code.setCodeSpace(name);
		code.setValue(val);
		building.setClazz(code);
	}
	
	private void setUsageAttr(Building building,String name,String val) {
		val=val.replaceAll("\"", "");
		val=BldgConstants.getUsageVal(val);
		Code code=new Code();
		code.setCodeSpace(name);
		code.setValue(val);
		List<Code> list=new ArrayList<>();
		list.add(code);
		building.setUsage(list);
	}
	
	private void setRoofAttr(Building building,String name,String val) {
		val=val.replaceAll("\"", "");
		val=BldgConstants.getRoofVal(val);
		Code code=new Code();
		code.setCodeSpace(name);
		code.setValue(val);
		building.setRoofType(code);
	}
	
}
