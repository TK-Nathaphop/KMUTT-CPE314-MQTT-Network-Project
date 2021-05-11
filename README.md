# MQTT-Network-Project
Implementation of Broker, Subscriber, and Publisher in MQTT by Java programming language

Our project there are 6 members
1. Nathaphop Sundarabhogin (Tonkla)
2. Natkanok Poksappaiboon (P)
3. Natthawat Tungruethaipak (Tong)
4. Thidarat Chaichana (Aey)
5. Phraewadee Chutirat (Phraewa)
6. Sirapitch Limpanithiphat (Peach)
===========================================================================================
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
