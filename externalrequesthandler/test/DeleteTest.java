import com.mscripts.templatebuilder.service.impl.TemplateBuilderServiceImpl;

public class DeleteTest {

	public static void main(String[] args) {
		try {
		System.out.println("hi ");
		TemplateBuilderServiceImpl templateBuilder = new TemplateBuilderServiceImpl();
		
			templateBuilder.buildCustomTemplate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("done");
			e.printStackTrace();
		}
	}

}
