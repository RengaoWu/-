import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Shop {
	private static int nDropsoff;//δ���ܷ����˳�������
	private int nBarbers;
	private int nChairs;
	private ArrayList<Barber> barList;
	private ArrayList<Customer> cusList;//�ȴ�����
	
	private Lock lock=new ReentrantLock();
	
	public int visitShop(int  id) throws InterruptedException {
		lock.lock();
		int barId;
		Barber barber;
		Customer customer=new Customer(id);
		if(cusList.size()>nChairs) {
			System.out.println("�˿�\t"+id+"\t�뿪��������Ϊû�п�λ����");
			nDropsoff++;
			lock.unlock();
			return -1;
		}
		//��ʦ���ڹ���
		if(getSleepBarber()==-1) {
			cusList.add(customer);//����ȴ�����
			System.out.println("�ͻ�\t"+id+"\t����,"+"\t������λ���� "+cusList.size());
			
			lock.unlock();
			customer.getLock().lock();
			customer.getCondition().await();//����
			customer.getLock().unlock();
			
			//��ʦ���û�
			lock.lock();
			barId=customer.getBar();
			barber=barList.get(barId);
			System.out.println("�˿� \t"+id+"\t�ߵ���ʦ\t\t"+barId);
		}else {
			//�û�����ʦ
			barId=getSleepBarber();//�ҵ���ʦ
			customer.setBarber(barId);
			barber=barList.get(barId);
			barber.setCustomer(customer);//������ʦ�Լ�
			barber.setBusy(true);//������ʦΪæµ
			System.out.println("�˿� \t"+id+"\t������ʦ\t\t"+barId);
		}
		
		lock.unlock();
		barber.getLock().lock();
		barber.getCondition().signalAll();
		barber.getLock().unlock();
		
		
		return barId;
	}
	public void leaveShop(int cusId,int barId) throws InterruptedException {
		lock.lock();
		Barber barber=barList.get(barId);
		Customer customer=barber.getCustomer();
		
		System.out.println("�˿�\t"+cusId+"\t�ȴ���ʦ\t\t"+barId+"\t�����");
		
		lock.unlock();
		//�ȴ���ʦ������
		customer.getLock().lock();
		customer.getCondition().await();
		customer.getLock().unlock();
		
		lock.lock();
		System.out.println("�ͻ�\t"+cusId+"\t�ش𡰺õġ�Ȼ���뿪");
		
		barber.getLock().lock();
		barber.getCondition().signalAll();//�뿪
		barber.getLock().unlock();
		lock.unlock();
	}
	public void helloCustomer(int id) throws InterruptedException {
		lock.lock();
		Barber barber=barList.get(id);
		Customer customer;
		barber.getLock().lock();
		//������û�й˿�
		if(cusList.size()==0) {
			System.out.println("��ʦ\t"+id+"\tȥ˯������Ϊû�пͻ�");
			barber.setBusy(false);
			
			lock.unlock();
			barber.getLock().lock();
			barber.getCondition().await();
			barber.getLock().unlock();
			
			lock.lock();
			customer=barber.getCustomer();
		}else {
			customer=cusList.get(0);
			cusList.remove(0);
			customer.setBarber(id);//�����û��Լ���λ��
			barber.setCustomer(customer);//�����Լ����û�
			
			lock.unlock();//�ͷ���
			customer.getLock().lock();;
			customer.getCondition().signalAll();//���������ϵĿͻ�
			customer.getLock().unlock();
			
			
			barber.getLock().lock();
			barber.getCondition().await();
			barber.getLock().unlock();
			//�ȴ��û��߹���
			lock.lock();
		}
		
		System.out.println("��ʦ\t"+id+"\t���ڷ���ͻ� \t"+customer.getId());
		
		lock.unlock();
	}
	public void byeCustomer(int id) throws InterruptedException {
		lock.lock();
		Barber barber=barList.get(id);
		Customer customer=barber.getCustomer();
		
		System.out.println("��ʦ\t"+id+"\t�����û� \t\t"+customer.getId()+"\t�������");
		
		lock.unlock();
		customer.getLock().lock();
		customer.getCondition().signalAll();//֪ͨ�ͻ�������
		customer.getLock().unlock();
		
		
		barber.getLock().lock();
		barber.getCondition().await();
		barber.getLock().unlock();
		lock.lock();
		System.out.println("��ʦ\t"+id+"\t����ɣ�������һ���û�");
		lock.unlock();
	}
	
	
	public void addDropsoff() {
		nDropsoff++;
	}
	public int getDropsoff() {
		return nDropsoff; 
	}
	public int getSleepBarber() {
		
		lock.lock();
		for(Barber b:barList) {
			if(b.getBusy()==false) {
				lock.unlock();
				return b.getId();
			}
		}
		lock.unlock();
		return -1;
	}
	
	public Shop(int b,int c) {
		nBarbers=b;
		nChairs=c;
		barList=new ArrayList<>();
		for(int i=0;i<nBarbers;i++) {
			barList.add(new Barber(i));
		}
		cusList=new ArrayList<>();
	}
}
