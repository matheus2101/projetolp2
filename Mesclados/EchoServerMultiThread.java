import java.net.*;
import java.io.*;

public class EchoServerMultiThread implements Runnable{

	Socket ss, ts;		//socket de receber mensagens e enviar mensagens para taxi server
	boolean sair;		//boolean para sair do loop principal
	boolean jaPediuTaxi;//boolean para nao pedir 300 taxis
	String resposta;	//resposta do servidor de taxi
	
	public EchoServerMultiThread(Socket ns, Socket ts){
		ss = ns;
		this.ts = ts;
		sair = false;
		jaPediuTaxi = false;
	}

	public void run() {
		try (	
				BufferedReader 		stdIn 	= new BufferedReader(new InputStreamReader(System.in));		//para ler o que vem do servidor de taxi
				DataOutputStream 	outTaxi	= new DataOutputStream(ts.getOutputStream());
				DataInputStream 	inTaxi 	= new DataInputStream(ts.getInputStream());					
				DataOutputStream 	out 	= new DataOutputStream(ss.getOutputStream());				//para lero que vem do cliente
				DataInputStream 	in 		= new DataInputStream(ss.getInputStream());            
				) {
				
			Pedido pedido = new Pedido();
			int cont = 0;
			boolean pedidoAndamento = true;
			
			while (true) {          
				
				String inputLine = in.readUTF();  		//le o que vem do cliente
				switch (inputLine)						//trata o que vem do cliente
				{
				case "menu":
					while(pedidoAndamento){
						out.writeUTF(pedido.exibeMenu()); 
						while(true){
							int id = Integer.parseInt(in.readUTF());
							
							if(id<0){
								out.writeUTF("\nId inv�lido, tente novamente!\n");
							} else if(cont>=30){
								out.writeUTF("\nN�mero m�ximo de compras atingidas!\n");
							} else if(id == 0){
								out.writeUTF("\nEtapa de compras finalizada!\n"
										+ "Deseja finalizar as compras?(s/n)\n");
								break;
							} else{
								out.writeUTF(pedido.fazerPedido(id));
								cont++;
							}
						}
						
						if("s".equals(in.readUTF())){		// Deseja fechar a conta
							out.writeUTF("Deseja entrega em domic�lio?(s/n)\n");
							if("s".equals(in.readUTF())){
								out.writeUTF("Digite o seu endereco de entrega: \n"); // Com ou sem Delivery
								out.writeUTF(pedido.entrega(in.readUTF()));
							}
								
							out.writeUTF(pedido.fecharPedido(in.readUTF())); // Encerramento da conta
							pedidoAndamento = false;
						}else{
							//break;
						}						
					}
					break;
				case "taxi":							//envia "request" e retorna ao cliente					
					if(!jaPediuTaxi)
					{
						outTaxi.writeUTF("request");
						resposta = inTaxi.readUTF();
						out.writeUTF(resposta);
						if(resposta.equals("taxi indo"))
						{
							jaPediuTaxi = true; //se taxi disponivel, nao pode pedir outro
						}
					}
					else													//se ja pediu e conseguiu, espere, mzr
					{
						out.writeUTF("Voce ja pediu um taxi, voce esta bebado!");
					}
					break;				
				
				case "bye": 
					sair = true;
					break;
				default:
					out.writeUTF("Mensagem invalida"); 
					break;	
				} 
				
				if (sair)
				{
					break;
				}
			}
			
			System.out.println("cliente saiu!");
			
			inTaxi.close();
			outTaxi.close();
			in.close();
			out.close();
			ss.close();                     

		} catch (IOException e) {
			System.out.println("cliente cagou-se!");
		}
	}
	//}
}