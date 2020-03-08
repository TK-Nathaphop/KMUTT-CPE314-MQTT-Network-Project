README
------------------------------------------------------------------------------------------------------------
# Our project implement Broker, Subscriber, and Publisher in MQTT by Java programming language
# There are 3 files: Broker.java, Publisher.java and Subscriber.java
# By group 4C
	Nathaphop Sundarabhogin 60070503420
	Natkanok Poksappaiboon 60070503421
	Natthawat Tungruethaipak 60070503426
	Thidarat Chaichana 60070503434
	Phraewadee Chutirat 60070503445
	Sirapitch Limpanithiphat 60070503500
------------------------------------------------------------------------------------------------------------
How to compile and run this program:

	1. "cd" to the folder that contain Broker.java, Publisher.java and Subscriber.java file.

	2. Compile all java files by terminal follow the command line 
		"javac *.java"

	3. Open server by run 'Broker.java' by follow the command line 
	   for print out messages received from Publisher and send it to Subscriber for that topic
		"java Broker"
	
	4. Typing command line "ip_address" for binding the ip address with the socket
		Example: 127.0.0.1

	5. Run 'Subscriber.java' by follow the command line 
		"java Subscriber"

	6. Typing command line "subscriber [ip] [topic]" for subscribes to given topic from Broker
		Example: subscribe 127.0.0.1 /topic

	7. Run 'Publisher.java' by follow the command line 
		"java Publisher"

	8. Typing command line "publish [ip] [topic] [data]" for publishes data to given topic
		Example: publish 127.0.0.1 /topic data

	9. Can exit from Subscriber and Publisher by follow the command line
		"exit"
	
	10. Can exit from Broker by Ctrl + c