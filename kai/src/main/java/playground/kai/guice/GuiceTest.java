package playground.kai.guice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public final class GuiceTest {
	private static final Logger log = Logger.getLogger( GuiceTest.class ) ;

	public static void main ( String [] args ) {
		new GuiceTest().run() ;
	}

//	@Inject Map<Annotation, Set<Provider<MyInterface>>> map ;

	void run() {
		List<Module> modules = new ArrayList<>() ;
		modules.add(  new AbstractModule(){
			@Override
			protected void configure(){
//				MapBinder<Annotation, MyInterface> mapBinder = MapBinder.newMapBinder( this.binder(), Annotation.class, MyInterface.class );
//				mapBinder.permitDuplicates() ;
//				mapBinder.addBinding( Names.named("abc") ).to( MyImpl1.class ) ;
//				mapBinder.addBinding(Names.named("abc") ).to( MyImpl2.class ) ;

				Multibinder<MyInterface> multiBinder = Multibinder.newSetBinder( this.binder(), MyInterface.class, Names.named( "someAnnotation" ) );;
//				Multibinder<MyInterface> multiBinder = Multibinder.newSetBinder( this.binder(), MyInterface.class );;
				multiBinder.permitDuplicates() ;
				multiBinder.addBinding().to( MyImpl1.class ) ;
				multiBinder.addBinding().to( MyImpl2.class ) ;

			}
		} ) ;
		Injector injector = Guice.createInjector( modules );

		Map<Key<?>, Binding<?>> bindings = injector.getAllBindings();

		for( Map.Entry<Key<?>, Binding<?>> entry : bindings.entrySet() ){
			log.info("") ;
			log.info( "key=" + entry.getKey() ) ;
			log.info( "value=" + entry.getValue() ) ;
		}
		log.info("") ;

		Collection<Provider<MyInterface>> set = injector.getInstance( Key.get( new TypeLiteral<Collection<Provider<MyInterface>>>(){} , Names.named( "someAnnotation" )) );

		//		Map<Annotation, Set<Provider<MyInterface>>> map = injector.getInstance( Key.get( new TypeLiteral<Map<Annotation, Set<Provider<MyInterface>>>>(){} ) );;
		//		Set<Provider<MyInterface>> set = map.get( Names.named("abc" ) ) ;

		for( Provider<MyInterface> provider : set ){
			provider.get() ;
		}

	}

	private interface MyInterface{

	}

	private static class MyImpl1 implements MyInterface{
		@Inject MyImpl1() {
			log.info( "ctor 1 called" );
		}
	}

	private static class MyImpl2 implements MyInterface{
		@Inject MyImpl2() {
			log.info( "ctor 2 called" );
		}
	}
}
