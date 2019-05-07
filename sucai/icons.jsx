//����[File]��[openDialog]��������ļ�ѡ�񴰿ڣ���ʾ�û�ѡ��1024*1024�ߴ��ͼ�꣬�����ļ��洢�ڱ���[bigIcon]�С�
var bigIcon = File.openDialog("��ѡ��һ��1024x1024��С��ͼƬ��", "*.png", false);

//���û�ѡ���ͼ���ļ��������򿪺���ĵ����������[pngDoc]��
var pngDoc = open(bigIcon, OpenDocumentType.PNG);

//����[Folder]��[selectDialog]��������ļ���ѡ�񴰿ڣ���ʾ�û�ѡ�����iOSͼ����ļ��С�
//�����ļ��д洢�ڱ���[destFolder]�С�
var destFolder = Folder.selectDialog( "��ѡ��һ��������ļ��У�");

//����һ�����飬��������ɸ���js������ɣ�ÿ��������һ��[name]���Ժ�[size]���ԣ��ֱ��ʾͼ������Ƶĳߴ硣
var icons = 
[
  {"name": "iTunesArtwork",        "size":1024},
  {"name": "Icon-512",        "size":512},
  {"name": "Icon",                      "size":57},
  {"name": "Icon@2x",                "size":114},
  {"name": "Icon-@2x",               "size":114},
  {"name": "Icon-40",                  "size":40},
  {"name": "Icon-72",                  "size":72},
  {"name": "Icon-72@2x",            "size":144},
  {"name": "Icon-Small",              "size":29},
  {"name": "Icon-Small@2x",       "size":58},
  {"name": "Icon-Small-50",         "size":50},
  {"name": "Icon-Small-50@2x",  "size":100},
  {"name": "logo-76",                  "size":76},
  {"name": "logo-80",                  "size":80},
  {"name": "logo-100",                "size":100},
  {"name": "logo-120",                "size":120},
  {"name": "logo-152",                "size":152}
];

//����һ������[option]����ʾiOS����ĸ�ʽΪPNG�����������PNGʱ��ִ��PNG8ѹ�����Ա�֤ͼ��������
var option = new PNGSaveOptions();

//���浱ǰ����ʷ״̬���Է�������ͼƬ���ٷ��������״̬�ĳߴ硣
option.PNG8 = false;
var startState = pngDoc.historyStates[0];

//���һ��ѭ����䣬������������ͼ���������顣
for (var i = 0; i < icons.length; i++) 
{
	//����һ������[icon]����ʾ��ǰ��������ͼ�����
	var icon = icons[i];

	//����[pngDoc]�����[resizeImage]��������ԭͼ�꣬��С����ǰ��������ͼ�������ĳߴ硣
	pngDoc.resizeImage(icon.size, icon.size);

	//����һ������[destFileName]����ʾҪ������ͼ������ơ�
	var destFileName = icon.name + ".png";

	if (icon.name == "iTunesArtwork")
        destFileName = icon.name;
    
	//����һ������[file]����ʾͼ�������·����
	var file = new File(destFolder + "/" + destFileName);

	//����[pngDoc]��[saveAs]����������С�ߴ���ͼ�굼����ָ��·����
	pngDoc.saveAs(file, option, true, Extension.LOWERCASE);

	//��[doc]�������ʷ״̬���ָ����ߴ�����֮ǰ��״̬�����ָ���1024*1024�ߴ磬Ϊ�´���С�ߴ���׼����
	pngDoc.activeHistoryState = startState;
}

//������ɺ󣬹ر��ĵ���
pngDoc.close(SaveOptions.DONOTSAVECHANGES);
