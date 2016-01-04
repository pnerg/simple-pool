[![Build Status](https://travis-ci.org/pnerg/simple-pool.svg)](https://travis-ci.org/pnerg/simple-pool) [![codecov.io](https://codecov.io/github/pnerg/simple-pool/coverage.svg?branch=master)](https://codecov.io/github/pnerg/simple-pool?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dmonix.pool/simple-pool/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/org.dmonix.pool/simple-pool) [![Javadoc](http://javadoc-badge.appspot.com/org.dmonix.pool/simple-pool.svg?label=javadoc)](http://javadoc-badge.appspot.com/org.dmonix.pool/simple-pool)  
# Simple Pool
Implements a thread safe pool for arbitrary objects.  
The basic principles of a pool is to create, hold, provide and possibly discard objects.  
Pools are meant to be used for object instances that are expensive to create, such as Sockets, Parsers, Validators.   
Pretty much anything related to I/O or mechanisms needing to read up data from disc is expensive to instantiate.  
So why discard the instance if it can be used mulitple times? But what if your code needs more than one instance to function?   
This is where the pool comes into play.
Features such as:
* Holds any arbitrary objects  
Your factory decides what type is shall hold
* Bounded size.  
You determine the maximum size of the pool
* Validation of instances returned to the pool (Optional).  
Instances failing the validation will be discarded and destroyed
* Destruction of idle instances (Optional)  
Configure a maximum time instances are allowed to sit idle before they are destroyed
* LIFO or FIFO mode  
Choose how instances are picked from the pool.  Last-In-First-Out or First-In-First-Out

## Why another implementation?
There are numerous of pool implementations out there.   
Heck I've written several of my own over the years.  
This one is meant to be what it's called, simple.  
Not a zillion of options creating complexity.   
Not a huge amount of code (unknown to you).  
Minimum amount of synchronized points, the bare minimum to be able to be thread safe.  
Written in and for Java 8 relying on Lambda expressions for neatness.
## The Factory
The _Factory_ is the starting point. It's used to create the Pool.  
The factory works according to a builder pattern where one starts by creating the factory _poolFor_ and finishes by invoking _create_.   
In between those invocations one may add optional behavior, such as the _ofSize_ (size) of the pool.  
Let's illustrate the creation of the factory with a few simple examples.  
The example below creates a pool factory for sockets.
```java
		Factory<Socket> factory = Factory.poolFor(() -> new Socket("127.0.0.1", 6969));
		factory = factory.ofSize(20);
		factory = factory.withValidator(socket -> socket.isConnected());
		factory = factory.withDestructor(socket -> {
			try {
				socket.close();
			}
			catch(Exception ex){}
		});
```
One can of course write it all in a single statements:
```java
	Factory<Socket> factory = Factory.poolFor(() -> new Socket("127.0.0.1", 6969)).ofSize(10).withValidator(s -> s.isConnected()).withDestructor(s -> {
		try {
			s.close();
		} catch (Exception ex) {
		}
	});
```

## The Pool
The _Pool_ is the holder of your instances.  
Once you have your _Factory_ you create pool instances using the _create_method.
```java
Pool<Socket> pool = factory.create();
```

